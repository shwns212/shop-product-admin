package com.jun.shop;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.shop.kafka.annotation.KafkaController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Flux;

public class TestClass {
	@Test
	public void tet() throws IOException, ClassNotFoundException {
		
		ClassPathScanningCandidateComponentProvider p = new ClassPathScanningCandidateComponentProvider(false);
		p.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
		p.findCandidateComponents("com.jun.shop").forEach(x -> {
			try {
				Class<?> c = Class.forName(x.getBeanClassName());
				Annotation[] annotations = c.getAnnotations();
				for(Annotation annotation : annotations) {
					if(annotation.annotationType().equals(KafkaController.class)) {
						System.out.println(c.getName());
					}
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println(x.getClass().getName());
		});
		
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//		String paht = "com.jun.shop.event.util";
//		Enumeration<URL> e = classLoader.getResources("com.jun.shop.event.util".replace(".", "/"));
//		while(e.hasMoreElements()) {
//			URL resource = e.nextElement();
//			File file = new File(resource.getFile());
//			System.out.println(file);
//			Class.forName(paht+"."+file.getName().substring(0, file.getName().length()-6));
//		}
		
	}
	
//	@Test
	public void d() {
		Flux<String> f = Flux.just("a","b").doOnSubscribe(v -> System.out.println("!!!"));;
		Flux<String> f2 = Flux.just("c","d").doOnSubscribe(v -> System.out.println("@@@"));;
		f.flatMap(x -> {
			System.out.println(x);
			return f2;
		})
		.subscribe();
	}

//	@Test
//	public void test() throws Exception {
//		ObjectMapper mapper = new ObjectMapper();
//		Event ev1 = new Event(1L, UserCreated.class.getSimpleName(), mapper.writeValueAsString(new UserCreated(1L, "junsoo", 29)));
//		// �̸� �ٲٱ�
//		Event ev2 = new Event(2L, NameChanged.class.getSimpleName(), mapper.writeValueAsString(new NameChanged("change junsoo")));
//		
//		Event ev3 = new Event(3L, AgeAdded.class.getSimpleName(), mapper.writeValueAsString(new AgeAdded(3)));
//		List<Event> list = Arrays.asList(ev1,ev2,ev3);
//		
//		User user = User.class.newInstance();
//		list.forEach(event -> {
//			// aggregate ����
//			try {
//				Method[] methods = user.getClass().getDeclaredMethods();
//				for(Method method : methods) {
//					Annotation[] annotations = method.getDeclaredAnnotations();
//					for(Annotation annotation : annotations) {
//						if(annotation.annotationType().equals(EventTrigger.class)) {
//							Class<?> c = ((EventTrigger) annotation).eventClass();
//							if(event.getType().equals(c.getSimpleName())) {
//								method.setAccessible(true);
//								method.invoke(user, mapper.readValue(event.getPayload(),c));
//							}
//						}
//					}
//				}
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//		});
//		System.out.println(user);
//		
//	}
//	@Test
	public void exec() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
//		Event ev1 = new Event(1L, UserCreated.class.getSimpleName(), mapper.writeValueAsString(new UserCreated(1L, "junsoo", 10)));
		// �̸� �ٲٱ�
		Event ev2 = new Event(2L, NameChanged.class.getSimpleName(),0L , mapper.writeValueAsString(new NameChanged("change junsoo")));
		
		Event ev3 = new Event(3L, AgeAdded.class.getSimpleName(),1L, mapper.writeValueAsString(new AgeAdded(2)));
		
		Event ev4 = new Event(3L, AgeAdded.class.getSimpleName(),2L, mapper.writeValueAsString(new AgeAdded(2)));
		List<Event> list = Arrays.asList(ev2,ev3,ev4);
		User user = eventReplay(1L, list, User.class);
		System.out.println(user);
	}
	
	// �ֱ� ������ ��ȸ 
	public <T extends AggregateRoot> Snapshot findRecentlySnapshot(Long aggregateId, Class<T> aggregateTypeClass) throws JsonMappingException, JsonProcessingException{
//		T t = clazz.newInstance();
		// snapshotRepository.find(aggregateId, clazz.getClass().getSimplName())
//		ObjectMapper mapper = new ObjectMapper();
//		String sql = "select * from snapshot where aggregate_type = :aggregateType and aggregate_id = :aggregateId order by version desc limit 1";
//		mapper.readValue("payload", aggregateTypeClass);
//		return (T) new User(1L,"jun", 5);
		return new Snapshot(1L,1L, aggregateTypeClass.getSimpleName(),3L, "");
	}
	
	
	// aggregate�� �̺�Ʈ �޼��忡 Ʈ������ annotation�� �ٿ��ش�.
	// �̺�Ʈ ����� ��ȸ�Ҷ� ������ ���� ��ȸ�Ѵ�. �ֱ׸���Ʈ Ÿ��, ���̵�� �˻� 
	public <T extends AggregateRoot>T eventReplay(Long aggregateId, List<Event> eventList, Class<T> aggregateTypeClass) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Snapshot snapshot = findRecentlySnapshot(aggregateId, aggregateTypeClass);
//		T aggregate = findRecentlySnapshot(aggregateId, aggregateTypeClass);
		T aggregate = null;
		if(snapshot == null) { // �������� ���� ��� ���ο� �ֱ׸���Ʈ ��ü �����ϰ� ��� �̺�Ʈ ��ȸ
			aggregate = aggregateTypeClass.newInstance();
//			eventList = findEventAll(aggregateTypeClass.getSimpleName(), aggregateId);
		}else { // �������� ������ ��� ������ ���̷ε带 ������ȭ�� �ֱ׸���Ʈ�� �����ϰ� ������ ������ �̺�Ʈ���� ��ȸ
			aggregate = mapper.readValue(snapshot.getPayload(), aggregateTypeClass);
//			eventList = findEventsAfterSnapshot();
			// eventList.stream().map(Event::getVersion).max()
			
			// ��ȸ�� �̺�Ʈ�� ���� ū ����
			Long maxEventVersion = eventList.stream().max(Comparator.comparing(Event::getVersion))
			.map(Event::getVersion)
			.get();
			if(Math.subtractExact(maxEventVersion, snapshot.getVersion()) >= 10) {
				// ������ ���
				saveSnapshot(aggregateId, aggregate);
			}
		}
		
		// �̺�Ʈ�� ���鼭 �ֽŻ��� ä���
		for(Event event : eventList) {
			// aggregate ����
			try {
				Method[] methods = aggregate.getClass().getDeclaredMethods();
				for(Method method : methods) {
					Annotation[] annotations = method.getDeclaredAnnotations();
					for(Annotation annotation : annotations) {
						if(annotation.annotationType().equals(EventHandler.class)) {
							if(method.getParameterCount() > 1) throw new Exception("�Ķ���ʹ� �� �ϳ�������"); // TODO ���� ���� �ؽ�Ʈ ����
							if(method.getParameterCount() == 0) throw new Exception("�̺�Ʈ Ÿ�� �Ķ���� �ϳ��� �����ؾ���"); // TODO ���� ���� �ؽ�Ʈ ����
							Class<?> eventClass = ((EventHandler) annotation).eventClass();
							if(event.getType().equals(eventClass.getSimpleName())) {
								method.setAccessible(true);
								method.invoke(aggregate, mapper.readValue(event.getPayload(), eventClass));
							}
						}
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		saveSnapshot(1L,aggregate);
		return aggregate;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Snapshot {
		private Long id;
		private Long eventId;
		private String type;
		private Long version;
		private String payload;
	}
	
	// �̺�Ʈ�� ���
	// �������� �̺�Ʈ�� ���� ���̸� ���� 10 �̻��̸� ������ ���
	public <T> void saveSnapshot(Long aggregateId, T aggregate) throws NoSuchFieldException, SecurityException {
		String type = aggregate.getClass().getSimpleName();
		System.out.println(type);
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class AggregateRoot {
		private Long id;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Event {
		private Long id;
		private String type;
		private Long version;
		private String payload;
	}
	
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class UserCreateCommand {
		private Long id;
		private String name;
		private Integer age; 
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	public static class User extends AggregateRoot{
		private Long id;
		private String name;
		private Integer age;
		
		public void userCreated(UserCreateCommand command) {
//			saveEvnet(new UserCreated(command.getId(), command.getName(), command.getAge()));
		}
		
		@EventHandler(eventClass = UserCreated.class)
		public void userCreated(UserCreated event) {
			this.name = event.name;
			this.age = event.age;
		}
		
		@EventHandler(eventClass = NameChanged.class)
		public void changeName(NameChanged event) {
			this.name = event.getName();
		}
		
		@EventHandler(eventClass = AgeAdded.class)
		public void addAge(AgeAdded event) {
			this.age += event.age;
		}
	}
	
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class UserCreated {
		private Long id;
		private String name;
		private Integer age;
	}
	
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class NameChanged{
		private String name;
	}
	
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AgeAdded{
		private Integer age;
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface EventHandler{
		Class<?> eventClass();
	} 
}
