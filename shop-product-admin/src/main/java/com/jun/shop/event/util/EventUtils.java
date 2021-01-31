package com.jun.shop.event.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.shop.event.EventHandler;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EventUtils {
	
	private final DatabaseClient dbClient;

	private final ObjectMapper objectMapper;
	
	private final EventRepository repository;
	
	private final SnapshotRepository snapshotRepository;
	
	private static final String DATA_NAME = "data"; 
	
	private static final String VERSION_NAME = "version";
	
	// 이벤트와 스냅샷의 차이
	@Value("${event.snapshot.distance.count:10}")
	private Long eventSnapshotDistanceCount ;
	
	/**
	 * 식별자 필드를 찾아서 반환한다.
	 * @param <T>
	 * @param event
	 * @return
	 */
	private <T> Field findIdentifierField(Class<T> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			Annotation[] annotations = field.getDeclaredAnnotations();
			for(Annotation annotation : annotations) {
				if(annotation.annotationType().equals(Identifier.class)) {
					Field idField = field;
					idField.setAccessible(true);
					return idField;
				}
			}
		}
		throw new NotExistIdentifierException("Not exist "+Identifier.class.getName()+" annotation in "+clazz.getName()+". this class must have @Identifier annotation.");
	}
	
	/**
	 * 이벤트를 저장한다.
	 * @param <T>
	 * @param aggregateId
	 * @param aggregateTypeClass
	 * @param event
	 * @return
	 */
	public <T> Mono<Event> saveEvent(T event, Class<?> aggregateTypeClass) {
		try {
			// 이벤트 객체에 들어있는 식별자 필드
			final Field idField = findIdentifierField(event.getClass());
			
			// aggregateType과 aggregateId로 가장 최근 이벤트를 조회한다.
			Mono<Event> recentlyEvent = repository.findRecentlyEvent(aggregateTypeClass.getSimpleName()
					, Optional.ofNullable((String) idField.get(event)).orElseGet(() -> ""));
			Mono<Event> result = recentlyEvent
			.switchIfEmpty(Mono.just(new Event()))
			.flatMap(x ->{
				try {
					// 식별자가 null이면 UUID로 값을 채운다. (처음 이벤트를 등록할때는 식별자가 없다.)
					String newId = idField.get(event) == null ? UUID.randomUUID().toString() : (String) idField.get(event); 
					idField.set(event, newId);
					// 조회한 데이터의 버전에 +1을 한다.
					Long version = Optional.ofNullable(x.getVersion()).orElseGet(() -> 0L) + 1;
					// 이벤트 본문 직렬화
					String payload = objectMapper.writeValueAsString(event);
					// 역직렬화가 가능한지 검사 불가능할 경우 catch로 빠짐
					objectMapper.readValue(payload, Map.class);
					// 값을 세팅하고
					Event newEvent = new Event(aggregateTypeClass.getSimpleName(), newId
							, event.getClass().getSimpleName(), version, payload, LocalDateTime.now());
					// 저장
					Mono<Event> saveEvent = repository.save(newEvent);
					// 스냅샷을 확인하고 저장
					saveSnapshot(newEvent, aggregateTypeClass);
					// 저장된 이벤트 반환
					return saveEvent;
				} catch (Exception e) {
					// JsonProcessingException은 복구 가능 예외지만 이벤트 저장 처리가 되면 안 되므로 복구 불가능 예외 처리 
					throw new FailedEventSaveException(e);
				}
			});
//			result.subscribe();
			return result;
		} catch (IllegalAccessException e) {
			throw new FailedEventSaveException(e);
		}
	}
//	@SuppressWarnings("unchecked")
//	public <T> Mono<Event> saveEvent(Class<?> aggregateTypeClass, T event) {
//		try {
//			Field f = event.getClass().getDeclaredField("id");
//			f.setAccessible(true);
//			String id = (String) f.get(event);
//			if(f.get(event) == null) {
//				id = UUID.randomUUID().toString();
//			}
//			f.set(event, id);
//		} catch (IllegalArgumentException | IllegalAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		// aggregateType과 aggregateId로 가장 최근 이벤트를 조회한다.
//		Mono<Event> recentlyEvent = repository.findRecentlyEvent(aggregateTypeClass.getSimpleName(), Optional.ofNullable(id).orElseGet(() -> ""));
//		Mono<Event> result = recentlyEvent.switchIfEmpty(Mono.just(new Event()))
//				.flatMap(x ->{
//					try {
//						// 첫번째 등록때 아이디를 생성한다.
//						// aggregateId가 널이면 새로 생성해서 저장
////				String newAggregateId = aggregateId;
////				if(ObjectUtils.isEmpty(newAggregateId)) {
////					newAggregateId = UUID.randomUUID().toString();
////				}
//						
////				T t = (T) event.getClass().newInstance();
////				Field f = event.getClass().getDeclaredField("id");
////				f.setAccessible(true);
////				String id = (String) f.get(event);
////				if(f.get(event) == null) {
////					id = UUID.randomUUID().toString();
////				}
////				f.set(event, id);
//						
//						
//						
//						// 조회한 데이터의 버전에 +1을 한다.
//						Long version = Optional.ofNullable(x.getVersion()).orElseGet(() -> 0L) + 1;
//						// 이벤트 본문 직렬화
//						String payload = objectMapper.writeValueAsString(event);
//						// 역직렬화가 가능한지 검사 불가능할 경우 catch로 빠짐
//						LinkedHashMap<String, Object> map = objectMapper.readValue(payload, LinkedHashMap.class);
////				map.clear();
////				map.put("id", aggregateId);
////				map.putAll(objectMapper.readValue(payload, LinkedHashMap.class));
////				payload = objectMapper.writeValueAsString(map);
//						// 값을 세팅하고
//						Event newEvent = new Event(aggregateTypeClass.getSimpleName(), id
//								, event.getClass().getSimpleName(), version, payload, LocalDateTime.now());
//						// 저장
//						Mono<Event> saveEvent = repository.save(newEvent);
//						// 스냅샷을 확인하고 저장
//						saveSnapshot(newEvent, aggregateTypeClass);
//						// 저장된 이벤트 반환
//						return saveEvent;
//					} catch (JsonProcessingException e) {
//						// JsonProcessingException은 복구 가능 예외지만 이벤트 저장 처리가 되면 안 되므로 복구 불가능 예외 처리 
//						throw new EventSaveFailException("Fail the event save", e);
//					} catch (IllegalAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (NoSuchFieldException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					return null;
//				});
//		result.subscribe();
//		return result;
//	}
	
	/**
	 * 스냅샷을 저장한다.
	 * @param <T>
	 * @param event
	 * @param aggregateTypeClass
	 */
	@SuppressWarnings("unchecked")
	public <T> void saveSnapshot(Event event, Class<T> aggregateTypeClass) {
		findAggregateAndVersion(aggregateTypeClass, event.getAggregateId())
		.subscribe(x -> {
			T data = (T) x.get(DATA_NAME);
			Long snapshotVersion = Optional.ofNullable((Long) x.get(VERSION_NAME)).orElseGet(() -> 0L);
			// 스냅샷과 이벤트의 버전 차이를 보고 특정 수치 이상 차이나면 스냅샷을 저장한다.
			if(Math.subtractExact(event.getVersion(), snapshotVersion) >= eventSnapshotDistanceCount) {
				try {
					// 최신 상태의 애그리거트를 직렬화 한다.
					String payload = objectMapper.writeValueAsString(data);
					// 스냅샷 객체를 생성한다.
					Snapshot newSnapshot = new Snapshot(event.getId(), aggregateTypeClass.getSimpleName(), event.getAggregateId()
							,event.getVersion(), payload, LocalDateTime.now());
					// 스냅샷 저장
					snapshotRepository.save(newSnapshot).subscribe();
				} catch (JsonProcessingException e) {
					throw new FailedEventSaveException(e);
				}
			}
		});
	}
	
	/**
	 * 최신 상태의 애그리거트를 반환한다.
	 * @param <T>
	 * @param aggregateTypeClass
	 * @param aggregateId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Mono<T> findAggregate(Class<T> aggregateTypeClass, String aggregateId) {
		// 가장 최근의 스냅샷을 조회한다.
		return (Mono<T>) findAggregateAndVersion(aggregateTypeClass, aggregateId)
				.map(x -> x.get(DATA_NAME));
	}
	
	/**
	 * 최신 상태의 애그리거트와 버전정보를 함꼐 반환한다.
	 * @param <T>
	 * @param aggregateTypeClass
	 * @param aggregateId
	 * @return
	 */
	public <T> Mono<Map<String, Object>> findAggregateAndVersion(Class<T> aggregateTypeClass, String aggregateId) {
		// 가장 최근의 스냅샷을 조회한다.
		return snapshotRepository.findRecentlySnapshot(aggregateTypeClass.getSimpleName(), aggregateId)
				.switchIfEmpty(Mono.just(new Snapshot()))
				.flatMap(snapshot -> {
					// 스냅샷 이후의 모든 이벤트들을 조회한다.
					Flux<Event> eventListFlux = repository.findAfterSnapshotEvents(aggregateTypeClass.getSimpleName()
							, aggregateId, Optional.ofNullable(snapshot.getVersion()).orElseGet(() -> 0L));
					
					try {
						T aggregate = ObjectUtils.isEmpty(snapshot.getPayload()) ? aggregateTypeClass.newInstance()
								: objectMapper.readValue(snapshot.getPayload(), aggregateTypeClass);
						
						// 애그리거트 식별자 정보를 세팅해준다.
						findIdentifierField(aggregateTypeClass);
						Field aggregateIdentifier = findIdentifierField(aggregateTypeClass);
						aggregateIdentifier.setAccessible(true);
						aggregateIdentifier.set(aggregate, aggregateId);

						// 이벤트들을 루프 돌리면서 애그리거트를 최신상태로 만든다.
						return eventListFlux.collectList()
								.map(eventList -> {
									// 최신 상태의 애그리거트
									Map<String, Object> resultMap = new HashMap<>();
									resultMap.put(DATA_NAME, eventReplay(eventList, aggregate));
									resultMap.put(VERSION_NAME, snapshot.getVersion());
									return resultMap;
								});
					}catch(Exception e) {
						e.printStackTrace();
						throw new RuntimeException();
					}
					
				});
	}
	
	
	
	// 이벤트를 등록
	// 스냅샷과 이벤트의 버전 차이를 보고 스냅샷 등록
	public <T> void saveSnapshot(Long eventId, Long version, Long aggregateId, T aggregate) {
		try {
//			new Snapshot(null, eventId, type, version, objectMapper.writeValueAsString(aggregate));
			String sql = "INSERT INTO snapshot (aggregate_type, aggregate_id, event_type, version, payload, created_at)"
					+ " VALUES(:aggregateType, :aggregateId, :eventType, :version, :payload, :createdAt)";
			dbClient.sql(sql)
			.bind("aggregateType", aggregate.getClass().getSimpleName())
			.bind("aggregateId", aggregateId)
			.bind("eventType", "")
			.bind("version", version)
			.bind("payload", objectMapper.writeValueAsString(aggregate))
			.bind("createdAt", LocalDateTime.now())
			.fetch()
			.rowsUpdated()
			.subscribe();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 특정 애그리거트의 최신 상태를 만들어서 반환한다.
	 * @param <T>
	 * @param eventList
	 * @param aggregate
	 * @return
	 */
	public <T> T eventReplay(List<Event> eventList, T aggregate) {
		// 이벤트를 돌면서 최신상태 채우기
		for(Event event : eventList) {
			// aggregate 생성
			try {
				boolean isExistEventType = false; // 이벤트 타입이 이벤트 스토어와 이벤트 핸들러 둘 다 존재하는지 체크 (중간에 이벤트 타입명을 변경하였을 경우)
				Method[] methods = aggregate.getClass().getDeclaredMethods();
				for(Method method : methods) {
					method.setAccessible(true);
					Annotation[] annotations = method.getDeclaredAnnotations();
					for(Annotation annotation : annotations) {
						if(annotation.annotationType().equals(EventHandler.class)) {
							if(method.getParameterCount() > 1) throw new NotOneParameterException();
							if(method.getParameterCount() == 0) throw new NotExistEventParameterException();
							Class<?> eventClass = ((EventHandler) annotation).eventClass();
							if(event.getEventType().equals(eventClass.getSimpleName())) {
								isExistEventType = true;
								method.invoke(aggregate, objectMapper.readValue(event.getPayload(), eventClass));
							}
						}
					}
				}
				String msg = "The event type '"+event.getEventType()+"' is not exist in class '"
						+aggregate.getClass().getSimpleName()+"'";
				if(!isExistEventType) throw new NotExistEventTypeException(msg);
			} catch(Exception e) {
				throw new FailedEventSaveException(e);
			}
		}
		return aggregate;
	}
	
}
