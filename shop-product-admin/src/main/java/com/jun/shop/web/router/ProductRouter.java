package com.jun.shop.web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jun.shop.web.handler.ProductHandler;

@Configuration
public class ProductRouter {
	@Bean
	public RouterFunction<ServerResponse> router(ProductHandler handler){
		return RouterFunctions.route()
		.route(RequestPredicates.POST("/products"), handler::createProduct)
		.route(RequestPredicates.PATCH("/price-change"), handler::changePrice)
		.build();
	}
}
