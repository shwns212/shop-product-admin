package com.jun.shop.web.service;

import com.jun.shop.model.command.ProductCommand.ChangePrice;
import com.jun.shop.model.command.ProductCommand.Create;

import reactor.core.publisher.Mono;

public interface ProductService {
	Mono<Long> createProduct(Mono<Create> command);

	Mono<String> changePrice(Mono<ChangePrice> bodyToMono);

	void changePrice(ChangePrice a);
}
