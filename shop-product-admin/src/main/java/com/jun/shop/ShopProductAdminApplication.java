package com.jun.shop;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import com.jun.message.listener.MessageListener;

@SpringBootApplication
public class ShopProductAdminApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(ShopProductAdminApplication.class, args);
//		new MessageListener("com.jun.shop", b).listen("com.jun.shop", b);
	}

	@Configuration
	public static class Config{
		
		@Autowired
		private GenericApplicationContext b;
		
		@PostConstruct
		public void test() {
			new MessageListener("com.jun.shop", b);
		}
	}
}
