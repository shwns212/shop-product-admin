package com.jun.shop.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ProductOption {
	private Integer optionNo;
	private List<Option> options; 
	private Integer additionalAmount;
	
	@Getter
	@AllArgsConstructor
	public static class Option {
		private String option;
	}
}
