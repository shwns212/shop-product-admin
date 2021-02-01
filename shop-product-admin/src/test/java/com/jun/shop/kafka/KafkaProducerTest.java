package com.jun.shop.kafka;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jun.shop.model.command.ProductCommand;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class KafkaProducerTest {

	
	@Autowired
	private KafkaTemplate<String, String> template;
	@Autowired
	private ObjectMapper mapper;
	
	@Test
	public void test() throws Exception {
//		ProductCommand.ChangePrice command = new ProductCommand.ChangePrice("86cad83f-9ab9-4f2b-962a-75669a788b3f", 200);
//		KafkaMessageTemplate template2 = new KafkaMessageTemplate("test", command);
//		KafkaRequest req = new KafkaRequest("test", mapper.writeValueAsString(command));
//		Map<String,Object> map = new HashMap<>();
//		map.put("type", "priceChange");
//		map.put("payload", command);
//		template.send("product", req.serialize());
////		template.send("product", mapper.writeValueAsString(template2));
//		System.out.println(template);
	}
}
