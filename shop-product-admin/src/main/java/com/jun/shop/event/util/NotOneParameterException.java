package com.jun.shop.event.util;

public class NotOneParameterException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotOneParameterException() {
		super("The method too many parameter. event handler method must have only one parameter");
	}
}
