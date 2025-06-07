package io.microprofile.tutorial.store.product.service;

import java.util.List;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.ProductRepository;
import jakarta.inject.Inject;

public class ProductService {

    @Inject
    private ProductRepository repository;

    public List<Product> findAllProducts() {
        return repository.findAllProducts();
    }
}