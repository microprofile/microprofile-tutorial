package io.microprofile.tutorial.graphql.product.repository;

import jakarta.enterprise.context.ApplicationScoped;
import io.microprofile.tutorial.graphql.product.dto.ProductInput;
import io.microprofile.tutorial.graphql.product.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repository layer for product persistence operations.
 */
@ApplicationScoped
public class ProductRepository {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ProductRepository() {
        initializeSampleData();
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public Product findById(Long id) {
        return products.get(id);
    }

    public List<Product> findByIds(List<Long> ids) {
        return ids.stream()
            .map(products::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }

    public boolean deleteById(Long id) {
        return products.remove(id) != null;
    }

    public int count() {
        return products.size();
    }

    public Long nextId() {
        return idCounter.getAndIncrement();
    }

    private void initializeSampleData() {
        seed(new ProductInput("Laptop", "High-performance laptop", 999.99, "Electronics", 50));
        seed(new ProductInput("Mouse", "Wireless mouse", 29.99, "Electronics", 150));
        seed(new ProductInput("Keyboard", "Mechanical keyboard", 89.99, "Electronics", 75));
        seed(new ProductInput("Monitor", "27-inch 4K monitor", 399.99, "Electronics", 30));
        seed(new ProductInput("Headphones", "Noise-canceling headphones", 199.99, "Electronics", 100));
    }

    private void seed(ProductInput input) {
        Long id = nextId();
        Product product = new Product(
            id,
            input.getName(),
            input.getDescription(),
            input.getPrice(),
            input.getCategory(),
            input.getStockQuantity(),
            java.time.LocalDate.now(),  // releaseDate - set to current date
            null,  // stockStatus will be computed by getAvailabilityStatus()
            "SKU-" + id,  // internalCode - excluded from GraphQL schema
            "Product created on " + java.time.LocalDate.now(),  // auditLog
            0.08  // taxRate - default 8% tax
        );
        save(product);
    }
}
