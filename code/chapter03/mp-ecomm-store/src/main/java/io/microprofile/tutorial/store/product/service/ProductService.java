package io.microprofile.tutorial.store.product.service;

import java.util.List;

import io.microprofile.tutorial.store.logging.Loggable;
import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.ProductRepository;
import jakarta.inject.Inject;

public class ProductService {

    @Inject
    private ProductRepository repository;

    @Loggable
    public List<Product> findAllProducts() {
        return repository.findAllProducts();
    }
}