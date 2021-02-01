package com.jun.shop.web.service.impl;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.jun.event.model.Event;
import com.jun.event.service.EventService;
import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.event.handler.ProductEventHandler;
import com.jun.shop.model.command.ProductCommand.ChangePrice;
import com.jun.shop.model.command.ProductCommand.Create;
import com.jun.shop.web.service.ProductService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductEventHandler eventHandler;
	private final EventService u;
//	private final ProjectionService projectionService;
	private final KafkaTemplate<String, String> templte;
	@Override
	public Mono<Long> createProduct(Mono<Create> command) {
		
		return command.map(x -> {
			Mono.just("a");
			Mono<Event> mono = new Product().create(x,u);
			mono
			.switchIfEmpty(Mono.just(new Event()))
			.subscribe(c -> {
				System.out.println(c);
			});
			return 1L;
		});
	}

	@Override
	public Mono<String> changePrice(Mono<ChangePrice> command) {
		return command.flatMap(x -> {
			Mono<Product> product = u.findAggregate(Product.class, x.getId());
			return product.map(m -> {
//				m.priceChanged(x, u)
//				.subscribe(c -> {
//					System.out.println(c);
//					projectionService.projection(c);
//				});
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
