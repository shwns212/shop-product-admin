package com.jun.shop.web.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jun.event.model.Event;
import com.jun.event.service.EventService;
import com.jun.message.message.Message;
import com.jun.message.sender.MessageSender;
import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.model.command.ProductCommand.ChangePrice;
import com.jun.shop.model.command.ProductCommand.Create;
import com.jun.shop.web.service.ProductService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final EventService u;
	private final MessageSender productMessageSender;
	@Override
	public Mono<UUID> createProduct(Mono<Create> command) {
		
		return command.flatMap(x -> {
			Mono<Event> mono = new Product().create(x,u);
			Mono<UUID> result = mono
			.doOnSuccess(c -> {
				Message message = new Message("createProduct", c);
				productMessageSender.send(message);
			})
			.map(c -> c.getAggregateId());
			return result;
		});
	}

	@Override
	public Mono<UUID> changePrice(Mono<ChangePrice> command) {
		return command.flatMap(x -> {
			Mono<Product> product = u.findAggregate(Product.class, x.getId());
			return product.map(m -> {
				m.priceChanged(x, u)
				.subscribe(c -> {
					System.out.println(c);
				});
				return m.getId();
			});
		});
	}

	@Override
	public void changePrice(ChangePrice command) {
		Mono<Product> product = u.findAggregate(Product.class, command.getId());
		product.doOnSuccess(x -> {
			x.priceChanged(command, u);
		})
		.subscribe();
	}

}
