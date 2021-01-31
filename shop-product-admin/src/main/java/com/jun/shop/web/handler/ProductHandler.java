package com.jun.shop.web.handler;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.model.command.ProductCommand;
import com.jun.shop.web.service.ProductService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {
	
	private final ProductService productService;
	
	public Mono<ServerResponse> createProduct(ServerRequest request) {
		return ServerResponse.created(URI.create("/"))
				.body(productService.createProduct(request.bodyToMono(ProductCommand.Create.class)), Product.class);
	}
	
	public Mono<ServerResponse> changePrice(ServerRequest request) {
		return ServerResponse.ok()
				.body(productService.changePrice(request.bodyToMono(ProductCommand.ChangePrice.class)), Void.class);
	}
}
