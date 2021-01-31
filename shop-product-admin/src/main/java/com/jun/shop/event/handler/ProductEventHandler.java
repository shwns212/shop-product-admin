package com.jun.shop.event.handler;

import org.springframework.stereotype.Component;

import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.event.repository.ProductEventStore;
import com.jun.shop.event.util.EventUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductEventHandler {
	
	private final ProductEventStore eventStore;
	private final EventUtils eu;
	public void create(Product product) {
		// 이벤트 저장
		
//		eu.apply(product.getId(), product.getVersion(), product.getClass(), product.getEvent());
//		eventStore.create(product.getId(), product.getVersion(), product.getEvent());
	}
}
