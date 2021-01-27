package com.jun.shop.web.service.impl;

import org.springframework.stereotype.Service;

import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.event.handler.ProductEventHandler;
import com.jun.shop.model.command.ProductCommand.Create;
import com.jun.shop.web.service.ProductService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductEventHandler eventHandler;
	
	@Override
	public Mono<Long> createProduct(Mono<Create> command) {
		
		
		// id 持失
		// aggregate 持失
		// event handle
		return command.map(x -> {
			Long id = 1L;
			Product product = Product.create(id, x);
			eventHandler.create(product);
			System.out.println(".asd");
			return id;
		});
	}

}
