package com.jun.shop.event.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventSchema {
	private Long id;
	private String type;
	private Long version;
	private String payload;
	private LocalDateTime createdAt;
}
