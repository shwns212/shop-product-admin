package com.jun.shop.event.model;


import com.jun.event.annotation.Identifier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PriceChanged {
	@Identifier
	private String id;
	private Integer amount;
}
