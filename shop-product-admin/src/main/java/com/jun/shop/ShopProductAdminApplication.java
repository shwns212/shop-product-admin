package com.jun.shop;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import com.jun.shop.kafka.annotation.KafkaController;

@SpringBootApplication
public class ShopProductAdminApplication {

	public static List<Object> kafkaControllers = new ArrayList<>();
//	public static List<Class<?>> kafkaControllers = new ArrayList<>();
	
	
	public static void main(String[] args) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
		provider.findCandidateComponents("com.jun.shop").forEach(cls -> {
			try {
				Class<?> c = Class.forName(cls.getBeanClassName());
				System.out.println(c);
				Annotation[] annotations = c.getAnnotations();
				for(Annotation annotation : annotations) {
					if(annotation.annotationType().equals(KafkaController.class)) {
//						ShopProductAdminApplication.kafkaControllers.add(c);
						kafkaControllers.add(c.newInstance());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		SpringApplication.run(ShopProductAdminApplication.class, args);
	}

}
