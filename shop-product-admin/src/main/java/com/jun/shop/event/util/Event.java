package com.jun.shop.event.util;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table("event")
@ToString
public class Event {
	@Id
	private Long id;
	private String aggregateType;
	private String aggregateId;
	private String eventType;
	private Long version;
	private String payload;
	private LocalDateTime createdAt;
	public Event(String aggregateType, String aggregateId, String eventType, Long version, String payload,
			LocalDateTime createdAt) {
		super();
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.version = version;
		this.payload = payload;
		this.createdAt = createdAt;
	}
}
