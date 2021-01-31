package com.jun.shop.domain.aggregate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jun.shop.domain.ProductOption;
import com.jun.shop.event.EventHandler;
import com.jun.shop.event.model.PriceChanged;
import com.jun.shop.event.model.ProductCreated;
import com.jun.shop.event.util.Event;
import com.jun.shop.event.util.EventUtils;
import com.jun.shop.event.util.Identifier;
import com.jun.shop.model.command.ProductCommand;
import com.jun.shop.model.command.ProductCommand.ChangePrice;
import com.jun.shop.model.command.ProductCommand.Create;
import com.jun.shop.model.command.ProductCommand.Create.ProductOptionCommand;

import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Getter
@NoArgsConstructor
public class Product {
	@Identifier
	private String id;
	private String name;
	private Integer price;
	private List<ProductOption> options;
	private String description;
	
	
	
	public Product(ProductCommand.Create command, EventUtils utils) {
		utils.saveEvent(new ProductCreated(command.getId(), command.getName(), command.getPrice(), command.getOptions(), command.getDescription())
				,this.getClass());
	}
//	public Product create(ProductCommand.Create command) {
//		EventUtils.saveEvent(this.id, 
//				this.getClass(),
//				new ProductCreated(null, command.getName(), command.getPrice(), command.getOptions(), command.getDescription()));
//		return new Product();
//	}

	@EventHandler(eventClass = ProductCreated.class)
	public void ProductCreated(ProductCreated event) {
//		this.id = event.getId();
		this.name = event.getName();
		this.price = event.getPrice();
		this.description = event.getDescription();
	}

	public Mono<Event> priceChanged(ChangePrice command, EventUtils utils) {
		return utils.saveEvent(new PriceChanged(command.getId(), command.getAmount()), this.getClass());
//		utils.saveEvent(command.getId(), this.getClass(), new PriceChanged(command.getId(), command.getAmount()));
	}
	
	@EventHandler(eventClass = PriceChanged.class)
	public void priceChanged(PriceChanged event) {
		this.price -= event.getAmount();
	}

	public Mono<Event> create(ProductCommand.Create command, EventUtils utils) {
		return utils.saveEvent(new ProductCreated(command.getId(), command.getName(), command.getPrice(), command.getOptions(), command.getDescription())
				,this.getClass());
	}


//	public Product(Long id, String name, Integer price, List<ProductOptionCommand> options, String description,
//			LocalDateTime now) {
//		super(id);
//		super.registEvent(new ProductCreated(id, name, price, options, description));
//	}
//
//
//
//
//	public static Mono<Product> create(Long id, Mono<Create> command) {
//		return command
//		.map(x -> new Product(id, x.getName(), x.getPrice(), x.getOptions(), x.getDescription(), LocalDateTime.now()));
//	}
//	public static Product create(Long id, Create command) {
//		return new Product(id, command.getName(), command.getPrice(), command.getOptions(), command.getDescription(), LocalDateTime.now());
//	}
	
}
