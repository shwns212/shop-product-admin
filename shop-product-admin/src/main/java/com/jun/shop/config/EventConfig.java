package com.jun.shop.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

import com.jun.event.service.EventService;

@Configuration
public class EventConfig {

	@Autowired
	private DatabaseClient dbClient;
	
	@Bean
	public EventService eventService() {
//		Map<String, String> config = new HashMap<>();
//		config.put("username", "junsoo");
//		config.put("password", "123");
//		config.put("database", "shop");
//		config.put("port", "30032");
//		config.put("host", "r2dbc:postgresql://182.209.85.228");
////		config.put("host", "r2dbc:postgresql://182.209.85.228:30032/shop");
//		return new EventService(dbClient);
		return new EventService(dbClient);
	}
}
