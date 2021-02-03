package com.jun.shop.message.handler;

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jca.context.SpringContextResourceAdapter;
import org.springframework.stereotype.Component;

import com.jun.message.annotation.MessageController;
import com.jun.message.annotation.MessageMapping;
import com.jun.message.message.Message;
import com.jun.shop.domain.aggregate.Product;
import com.jun.shop.repository.ProductMongoRepository;
import com.jun.shop.web.service.ProductService;

import lombok.NoArgsConstructor;

@Component
@MessageController(topic = "product", groupId = "event", bootstrapServers = {"182.209.85.228:9092"})
@NoArgsConstructor
public class ProductMessageHandler {

	@Autowired
	private ProductMongoRepository mongoRepository;
	@Autowired
	private ProductService service;
	@Autowired
	private ApplicationContext con;
	
	@MessageMapping(value = "projection")
	public void projection(Message message) {
		System.out.println(con);
		Product product = message.bodyToObject(Product.class);
		mongoRepository.save(product);
	}
}
