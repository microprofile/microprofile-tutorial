package io.microprofile.tutorial.store.product.service;

import java.util.List;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

import io.microprofile.tutorial.store.product.repository.ProductRepository;
import io.microprofile.tutorial.store.product.entity.Product;

@RequestScoped
public class ProductService {
    
    @Inject
    ProductRepository productRepository;

    public List<Product> getProducts() {
        return productRepository.findAllProducts();
    }

    public Product getProduct(Long id) {
        return productRepository.findProductById(id);
    }

    public void createProduct(Product product) {
        productRepository.createProduct(product);
    }

    public Product updateProduct(Product product) {
        return productRepository.updateProduct(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteProduct(productRepository.findProductById(id));
    }
}
