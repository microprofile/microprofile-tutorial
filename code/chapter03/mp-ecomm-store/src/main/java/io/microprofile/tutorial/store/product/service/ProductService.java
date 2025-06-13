package io.microprofile.tutorial.store.product.service;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
@Logged
public class ProductService {
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());
    private List<Product> products = new ArrayList<>();

    public ProductService() {
        // Initialize the list with some sample products
        products.add(new Product(1L, "iPhone", "Apple iPhone 15", 999.99));
        products.add(new Product(2L, "MacBook", "Apple MacBook Air", 1299.0));
    }

    public List<Product> getAllProducts() {
        LOGGER.info("Fetching all products");
        return products;
    }

    public Optional<Product> getProductById(Long id) {
        LOGGER.info("Fetching product with id: " + id);
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Product createProduct(Product product) {
        LOGGER.info("Creating product: " + product);
        products.add(product);
        return product;
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        LOGGER.info("Updating product with id: " + id);
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.getId().equals(id)) {
                product.setName(updatedProduct.getName());
                product.setDescription(updatedProduct.getDescription());
                product.setPrice(updatedProduct.getPrice());
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    public boolean deleteProduct(Long id) {
        LOGGER.info("Deleting product with id: " + id);
        Optional<Product> product = getProductById(id);
        if (product.isPresent()) {
            products.remove(product.get());
            return true;
        }
        return false;
    }
}
