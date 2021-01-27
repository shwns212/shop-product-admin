package com.jun.shop.event.model;

import lombok.Getter;

@Getter
public class Event {
	private Long id;
	public Event(Long id) {
		this.id = id;	
	}
}
