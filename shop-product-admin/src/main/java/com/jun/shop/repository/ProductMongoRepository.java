package com.jun.shop.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.jun.shop.domain.aggregate.Product;

public interface ProductMongoRepository extends ReactiveMongoRepository<Product, UUID> {

}
