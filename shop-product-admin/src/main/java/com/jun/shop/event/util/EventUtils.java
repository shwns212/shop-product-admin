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
	
	// �̺�Ʈ�� �������� ����
	@Value("${event.snapshot.distance.count:10}")
	private Long eventSnapshotDistanceCount ;
	
	/**
	 * �ĺ��� �ʵ带 ã�Ƽ� ��ȯ�Ѵ�.
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
	 * �̺�Ʈ�� �����Ѵ�.
	 * @param <T>
	 * @param aggregateId
	 * @param aggregateTypeClass
	 * @param event
	 * @return
	 */
	public <T> Mono<Event> saveEvent(T event, Class<?> aggregateTypeClass) {
		try {
			// �̺�Ʈ ��ü�� ����ִ� �ĺ��� �ʵ�
			final Field idField = findIdentifierField(event.getClass());
			
			// aggregateType�� aggregateId�� ���� �ֱ� �̺�Ʈ�� ��ȸ�Ѵ�.
			Mono<Event> recentlyEvent = repository.findRecentlyEvent(aggregateTypeClass.getSimpleName()
					, Optional.ofNullable((String) idField.get(event)).orElseGet(() -> ""));
			Mono<Event> result = recentlyEvent
			.switchIfEmpty(Mono.just(new Event()))
			.flatMap(x ->{
				try {
					// �ĺ��ڰ� null�̸� UUID�� ���� ä���. (ó�� �̺�Ʈ�� ����Ҷ��� �ĺ��ڰ� ����.)
					String newId = idField.get(event) == null ? UUID.randomUUID().toString() : (String) idField.get(event); 
					idField.set(event, newId);
					// ��ȸ�� �������� ������ +1�� �Ѵ�.
					Long version = Optional.ofNullable(x.getVersion()).orElseGet(() -> 0L) + 1;
					// �̺�Ʈ ���� ����ȭ
					String payload = objectMapper.writeValueAsString(event);
					// ������ȭ�� �������� �˻� �Ұ����� ��� catch�� ����
					objectMapper.readValue(payload, Map.class);
					// ���� �����ϰ�
					Event newEvent = new Event(aggregateTypeClass.getSimpleName(), newId
							, event.getClass().getSimpleName(), version, payload, LocalDateTime.now());
					// ����
					Mono<Event> saveEvent = repository.save(newEvent);
					// �������� Ȯ���ϰ� ����
					saveSnapshot(newEvent, aggregateTypeClass);
					// ����� �̺�Ʈ ��ȯ
					return saveEvent;
				} catch (Exception e) {
					// JsonProcessingException�� ���� ���� �������� �̺�Ʈ ���� ó���� �Ǹ� �� �ǹǷ� ���� �Ұ��� ���� ó�� 
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
//		// aggregateType�� aggregateId�� ���� �ֱ� �̺�Ʈ�� ��ȸ�Ѵ�.
//		Mono<Event> recentlyEvent = repository.findRecentlyEvent(aggregateTypeClass.getSimpleName(), Optional.ofNullable(id).orElseGet(() -> ""));
//		Mono<Event> result = recentlyEvent.switchIfEmpty(Mono.just(new Event()))
//				.flatMap(x ->{
//					try {
//						// ù��° ��϶� ���̵� �����Ѵ�.
//						// aggregateId�� ���̸� ���� �����ؼ� ����
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
//						// ��ȸ�� �������� ������ +1�� �Ѵ�.
//						Long version = Optional.ofNullable(x.getVersion()).orElseGet(() -> 0L) + 1;
//						// �̺�Ʈ ���� ����ȭ
//						String payload = objectMapper.writeValueAsString(event);
//						// ������ȭ�� �������� �˻� �Ұ����� ��� catch�� ����
//						LinkedHashMap<String, Object> map = objectMapper.readValue(payload, LinkedHashMap.class);
////				map.clear();
////				map.put("id", aggregateId);
////				map.putAll(objectMapper.readValue(payload, LinkedHashMap.class));
////				payload = objectMapper.writeValueAsString(map);
//						// ���� �����ϰ�
//						Event newEvent = new Event(aggregateTypeClass.getSimpleName(), id
//								, event.getClass().getSimpleName(), version, payload, LocalDateTime.now());
//						// ����
//						Mono<Event> saveEvent = repository.save(newEvent);
//						// �������� Ȯ���ϰ� ����
//						saveSnapshot(newEvent, aggregateTypeClass);
//						// ����� �̺�Ʈ ��ȯ
//						return saveEvent;
//					} catch (JsonProcessingException e) {
//						// JsonProcessingException�� ���� ���� �������� �̺�Ʈ ���� ó���� �Ǹ� �� �ǹǷ� ���� �Ұ��� ���� ó�� 
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
	 * �������� �����Ѵ�.
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
			// �������� �̺�Ʈ�� ���� ���̸� ���� Ư�� ��ġ �̻� ���̳��� �������� �����Ѵ�.
			if(Math.subtractExact(event.getVersion(), snapshotVersion) >= eventSnapshotDistanceCount) {
				try {
					// �ֽ� ������ �ֱ׸���Ʈ�� ����ȭ �Ѵ�.
					String payload = objectMapper.writeValueAsString(data);
					// ������ ��ü�� �����Ѵ�.
					Snapshot newSnapshot = new Snapshot(event.getId(), aggregateTypeClass.getSimpleName(), event.getAggregateId()
							,event.getVersion(), payload, LocalDateTime.now());
					// ������ ����
					snapshotRepository.save(newSnapshot).subscribe();
				} catch (JsonProcessingException e) {
					throw new FailedEventSaveException(e);
				}
			}
		});
	}
	
	/**
	 * �ֽ� ������ �ֱ׸���Ʈ�� ��ȯ�Ѵ�.
	 * @param <T>
	 * @param aggregateTypeClass
	 * @param aggregateId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Mono<T> findAggregate(Class<T> aggregateTypeClass, String aggregateId) {
		// ���� �ֱ��� �������� ��ȸ�Ѵ�.
		return (Mono<T>) findAggregateAndVersion(aggregateTypeClass, aggregateId)
				.map(x -> x.get(DATA_NAME));
	}
	
	/**
	 * �ֽ� ������ �ֱ׸���Ʈ�� ���������� �Բ� ��ȯ�Ѵ�.
	 * @param <T>
	 * @param aggregateTypeClass
	 * @param aggregateId
	 * @return
	 */
	public <T> Mono<Map<String, Object>> findAggregateAndVersion(Class<T> aggregateTypeClass, String aggregateId) {
		// ���� �ֱ��� �������� ��ȸ�Ѵ�.
		return snapshotRepository.findRecentlySnapshot(aggregateTypeClass.getSimpleName(), aggregateId)
				.switchIfEmpty(Mono.just(new Snapshot()))
				.flatMap(snapshot -> {
					// ������ ������ ��� �̺�Ʈ���� ��ȸ�Ѵ�.
					Flux<Event> eventListFlux = repository.findAfterSnapshotEvents(aggregateTypeClass.getSimpleName()
							, aggregateId, Optional.ofNullable(snapshot.getVersion()).orElseGet(() -> 0L));
					
					try {
						T aggregate = ObjectUtils.isEmpty(snapshot.getPayload()) ? aggregateTypeClass.newInstance()
								: objectMapper.readValue(snapshot.getPayload(), aggregateTypeClass);
						
						// �ֱ׸���Ʈ �ĺ��� ������ �������ش�.
						findIdentifierField(aggregateTypeClass);
						Field aggregateIdentifier = findIdentifierField(aggregateTypeClass);
						aggregateIdentifier.setAccessible(true);
						aggregateIdentifier.set(aggregate, aggregateId);

						// �̺�Ʈ���� ���� �����鼭 �ֱ׸���Ʈ�� �ֽŻ��·� �����.
						return eventListFlux.collectList()
								.map(eventList -> {
									// �ֽ� ������ �ֱ׸���Ʈ
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
	
	
	
	// �̺�Ʈ�� ���
	// �������� �̺�Ʈ�� ���� ���̸� ���� ������ ���
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
	 * Ư�� �ֱ׸���Ʈ�� �ֽ� ���¸� ���� ��ȯ�Ѵ�.
	 * @param <T>
	 * @param eventList
	 * @param aggregate
	 * @return
	 */
	public <T> T eventReplay(List<Event> eventList, T aggregate) {
		// �̺�Ʈ�� ���鼭 �ֽŻ��� ä���
		for(Event event : eventList) {
			// aggregate ����
			try {
				boolean isExistEventType = false; // �̺�Ʈ Ÿ���� �̺�Ʈ ������ �̺�Ʈ �ڵ鷯 �� �� �����ϴ��� üũ (�߰��� �̺�Ʈ Ÿ�Ը��� �����Ͽ��� ���)
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
