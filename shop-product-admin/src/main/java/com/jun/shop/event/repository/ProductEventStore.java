package com.jun.shop.event.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.shop.event.model.Event;
import com.jun.shop.event.model.EventSchema;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductEventStore {
	
	private final ObjectMapper objectMapper;
	
	public void create(Long id, Long version, Event evnet) {
		try {
			new EventSchema(id, evnet.getClass().toString(), version, objectMapper.writeValueAsString(evnet), LocalDateTime.now());
			// ¿˙¿Â
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
