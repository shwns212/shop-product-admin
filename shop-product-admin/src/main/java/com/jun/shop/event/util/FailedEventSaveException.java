package com.jun.shop.event.util;

public class FailedEventSaveException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FailedEventSaveException(Throwable throwable) {
		super("Fail the event save", throwable);
	}
	
}
