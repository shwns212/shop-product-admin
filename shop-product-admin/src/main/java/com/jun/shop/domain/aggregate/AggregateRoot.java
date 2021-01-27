package com.jun.shop.domain.aggregate;

import com.jun.shop.event.model.Event;

import lombok.Getter;

@Getter
public class AggregateRoot {
	private Long id;
	private Long version = 0L;
	private Event event;
	AggregateRoot(Long id){
		this.id = id;
	}
	
	protected void registEvent(Event event) {
		this.event = event;
	}
}
