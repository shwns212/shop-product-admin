package com.jun.shop.web.service;

import java.util.UUID;

import com.jun.shop.model.command.ProductCommand.ChangePrice;
import com.jun.shop.model.command.ProductCommand.Create;

import reactor.core.publisher.Mono;

public interface ProductService {
	Mono<UUID> createProduct(Mono<Create> command);

	Mono<UUID> changePrice(Mono<ChangePrice> bodyToMono);

	void changePrice(ChangePrice a);
}
