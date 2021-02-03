package com.jun.shop.web.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jun.event.service.EventService;
import com.jun.message.message.Message;
import com.jun.message.sender.MessageSender;
import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.model.command.ProductCommand.ChangePrice;
import com.jun.shop.model.command.ProductCommand.Create;
import com.jun.shop.repository.ProductMongoRepository;
import com.jun.shop.web.service.ProductService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final EventService eventService;
	private final MessageSender productMessageSender;
	private final ProductMongoRepository repo;
	@Override
	public Mono<UUID> createProduct(Mono<Create> command) {
		
		return command.flatMap(x -> {
			return new Product().create(x,eventService)
			.doOnSuccess(c -> {
				eventService.findAggregate(Product.class, c.getAggregateId())
				.doOnSuccess(data -> {
					productMessageSender.send(new Message("projection", data));
				}).subscribe();
			})
			.map(c -> c.getAggregateId());
		});
	}

	@Override
	public Mono<UUID> changePrice(Mono<ChangePrice> command) {
		return command.flatMap(x -> {
			Mono<Product> product = eventService.findAggregate(Product.class, x.getId());
			return product.map(m -> {
				m.priceChanged(x, eventService)
				.subscribe(c -> {
					System.out.println(c);
				});
				return m.getId();
			});
		});
	}

	@Override
	public void changePrice(ChangePrice command) {
		Mono<Product> product = eventService.findAggregate(Product.class, command.getId());
		product.doOnSuccess(x -> {
			x.priceChanged(command, eventService);
		})
		.subscribe();
	}
	
	public void findAll() {
		repo.findAll(); 
	}

}
