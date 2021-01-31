package com.jun.shop.kafka;

import com.jun.shop.kafka.annotation.KafkaController;
import com.jun.shop.model.command.ProductCommand;

@KafkaController(bootStrapServers = {"192.168.219.104:9092"}, groupId = "event", topic = "product")
public class KafkaMessageController {
	@KafkaMapping(value = "priceChange")
	public void test(KafkaRequest request) {
		ProductCommand.ChangePrice tem = request.bodyToObject(ProductCommand.ChangePrice.class);
		
		System.out.println("mapping!!!");
		System.out.println(request);
	}
	
	@KafkaMapping(value = "test")
	public void test2(KafkaRequest request) {
		ProductCommand.ChangePrice tem = request.bodyToObject(ProductCommand.ChangePrice.class);
		
		System.out.println("mapping!!!");
		System.out.println(request);
	}
}
