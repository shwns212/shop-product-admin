package com.jun.shop.event.util;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table("snapshot")
public class Snapshot {
	@Id
	private Long id;
	private Long eventId;
	private String aggregateType;
	private String aggregateId;
	private Long version;
	private String payload;
	private LocalDateTime createdAt;
	public Snapshot(Long eventId, String aggregateType, String aggregateId, Long version, String payload,
			LocalDateTime createdAt) {
		super();
		this.eventId = eventId;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.version = version;
		this.payload = payload;
		this.createdAt = createdAt;
	}
}
