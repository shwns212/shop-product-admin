package com.jun.shop.kafka;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.util.Strings;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.shop.ShopProductAdminApplication;
import com.jun.shop.kafka.annotation.KafkaController;

import lombok.RequiredArgsConstructor;

@EnableKafka
@Component
@RequiredArgsConstructor
public class Listener {
	
	private final ObjectMapper objectMapper;
	
//	@KafkaListener(groupId = "event",topics = {"product"})
//	@PostConstruct
	public void listen() {
		handle();
	}
	public void handle() {
		try {
//			KafkaMessageTemplate template = objectMapper.readValue(message, KafkaMessageTemplate.class);
			
//			KafkaController handler = KafkaController.class.newInstance();
			for(Object controller : ShopProductAdminApplication.kafkaControllers) {
				Annotation[] cAnnotations = controller.getClass().getAnnotations();
				for(Annotation cAnnotation : cAnnotations) {
					if(cAnnotation.annotationType().equals(KafkaController.class)) {
						new Thread(() ->  {
							
							String[] bootStrapServers = ((KafkaController)cAnnotation).bootStrapServers(); 
							String topic = ((KafkaController)cAnnotation).topic();
							String groupId = ((KafkaController)cAnnotation).groupId();
							
							// 카프카 기본 설정
							Properties properties = new Properties();
							properties.put("bootstrap.servers",String.join(",", Arrays.asList(bootStrapServers)));
							properties.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
							properties.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
							properties.put("group.id", groupId);
							
							// 컨슈머 생성
							KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
							// 구독
							consumer.subscribe(Collections.singleton(topic));
							
							
							
							while(true) {
								ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
								for(ConsumerRecord<String, String> record : records) {
									String message = record.value();
									System.out.println(message);
									KafkaMessageTemplate template = null;
									try {
										template = objectMapper.readValue(message, KafkaMessageTemplate.class);
									} catch (JsonProcessingException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									Method[] methods = controller.getClass().getDeclaredMethods();
									for(Method method : methods) {
										Annotation[] annotations = method.getDeclaredAnnotations();
										for(Annotation annotation : annotations) {
											if(annotation.annotationType().equals(KafkaMapping.class)) {
												String value = ((KafkaMapping)annotation).value();
												if(template.getType().equals(value)) {
													try {
														method.invoke(controller, new KafkaRequest(message, objectMapper.writeValueAsString(template.getBody())));
													} catch (IllegalAccessException | IllegalArgumentException
															| InvocationTargetException | JsonProcessingException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
											}
										}
									}
								}
							}
						},"jun-kafka").start();
					}
				}
				
				
//				Method[] methods = controller.getClass().getDeclaredMethods();
//				for(Method method : methods) {
//					Annotation[] annotations = method.getDeclaredAnnotations();
//					for(Annotation annotation : annotations) {
//						if(annotation.annotationType().equals(KafkaMapping.class)) {
//							String value = ((KafkaMapping)annotation).value();
//							if(template.getType().equals(value)) {
//								method.invoke(controller, new KafkaRequest(value, objectMapper.writeValueAsString(template.getPayload())));
//							}
//						}
//					}
//				}
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
