package com.jun.shop.event.handler;

import org.springframework.stereotype.Component;

import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.event.repository.ProductEventStore;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductEventHandler {
	
	private final ProductEventStore eventStore;
	
	public void create(Product product) {
		// 이벤트 저장
		eventStore.create(product.getId(), product.getVersion(), product.getEvent());
	}
}
