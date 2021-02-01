package com.jun.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jun.message.sender.MessageSender;

@Configuration
public class MessageConfig {

	@Bean(name = "productMessageSender")
	public MessageSender productMessageSender() {
		MessageSender sender = new MessageSender("product", "192.168.219.104:9092");
		return sender;
	}
}
