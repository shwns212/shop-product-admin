package com.jun.shop.event.util;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectionService {
	
	private final KafkaTemplate<String, String> template;
	private final ObjectMapper objectMapper;
	
	private static final String PRODUCT_TOPIC_NAME = "product";
	public void projection(Object message) {
		try {
			template.send(PRODUCT_TOPIC_NAME, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
