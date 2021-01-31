package com.jun.shop.model.command;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ProductCommand {

	@Getter
	public static class Create {
		private String id;
		private String name;
		private Integer price;
		private List<ProductOptionCommand> options;
		private String description; 

		@Getter
		public static class ProductOptionCommand {
			private Integer optionNo;
			private List<Option> options; 
			private Integer additionalAmount;
			
			@Getter
			public static class Option {
				private String option;
			}
		}
	}
	
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ChangePrice {
		private String id;
		private Integer amount;
	}
}
