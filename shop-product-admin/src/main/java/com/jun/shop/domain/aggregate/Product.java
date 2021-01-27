package com.jun.shop.domain.aggregate;

import java.time.LocalDateTime;
import java.util.List;

import com.jun.shop.domain.ProductOption;
import com.jun.shop.event.model.ProductCreated;
import com.jun.shop.model.command.ProductCommand;
import com.jun.shop.model.command.ProductCommand.Create;
import com.jun.shop.model.command.ProductCommand.Create.ProductOptionCommand;

import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
public class Product extends AggregateRoot {
	private String name;
	private Integer price;
	private List<ProductOption> options;
	private String description;
	
	
	
	


	public Product(Long id, String name, Integer price, List<ProductOptionCommand> options, String description,
			LocalDateTime now) {
		super(id);
		super.registEvent(new ProductCreated(id, name, price, options, description));
	}




	public static Mono<Product> create(Long id, Mono<Create> command) {
		return command
		.map(x -> new Product(id, x.getName(), x.getPrice(), x.getOptions(), x.getDescription(), LocalDateTime.now()));
	}
	public static Product create(Long id, Create command) {
		return new Product(id, command.getName(), command.getPrice(), command.getOptions(), command.getDescription(), LocalDateTime.now());
	}
	
}
