package com.jun.shop.event.model;

import java.util.ArrayList;
import java.util.List;

import com.jun.shop.event.model.ProductCreated.ProductOption.Option;
import com.jun.shop.model.command.ProductCommand.Create.ProductOptionCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ProductCreated extends Event {
	
	private String name;
	private Integer price;
	private List<ProductOption> options;
	private String description;
	
	@Getter
	@AllArgsConstructor
	public static class ProductOption {
		private Integer optionNo;
		private List<Option> options; 
		private Integer additionalAmount;
		
		@Getter
		@AllArgsConstructor
		public static class Option {
			private String option;
		}
	}

	public ProductCreated(Long id, String name, Integer price, List<ProductOptionCommand> options, String description) {
		super(id);
		this.name = name;
		this.price = price;
		this.description = description;
		
		List<ProductOption> eo = new ArrayList<>();
		int optionNo = 1;
		for(ProductOptionCommand optionCommand : options) {
			List<ProductOption.Option> eventOptions = new ArrayList<>();
			List<com.jun.shop.model.command.ProductCommand.Create.ProductOptionCommand.Option> optionDetails = optionCommand.getOptions();
			for(com.jun.shop.model.command.ProductCommand.Create.ProductOptionCommand.Option s : optionDetails) {
				eventOptions.add(new Option(s.getOption()));
			}
			ProductOption productOption = new ProductOption(optionNo, eventOptions, optionCommand.getAdditionalAmount());
			eo.add(productOption);
			optionNo++;
		}
		this.options = eo;
	}

}
