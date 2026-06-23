package io.microprofile.tutorial.graphql.product.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.microprofile.tutorial.graphql.product.dto.ProductInput;
import io.microprofile.tutorial.graphql.product.entity.Product;
import io.microprofile.tutorial.graphql.product.exception.ProductNotFoundException;
import io.microprofile.tutorial.graphql.product.repository.ProductRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service layer for product operations
 */
@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    @ConfigProperty(name = "product.max.results", defaultValue = "100")
    Integer maxResults;
    
    public List<Product> findAll() {
        long limit = maxResults != null && maxResults > 0 ? maxResults : 100;
        return productRepository.findAll().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<Product> getProducts(Integer limit) {
        long maxLimit = limit != null && limit > 0 ? limit : 100;
        return productRepository.findAll().stream()
            .limit(maxLimit)
            .collect(Collectors.toList());
    }
    
    public Product findById(Long id) {
        return productRepository.findById(id);
    }
    
    public List<Product> findByIds(List<Long> ids) {
        return productRepository.findByIds(ids).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<Product> search(String searchTerm, String category) {
        return productRepository.findAll().stream()
            .filter(p -> {
                boolean matchesSearch = searchTerm == null || 
                    p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchTerm.toLowerCase()));
                boolean matchesCategory = category == null || category.equals(p.getCategory());
                return matchesSearch && matchesCategory;
            })
            .collect(Collectors.toList());
    }
    
    public Product createProduct(ProductInput input) {
        Long id = productRepository.nextId();
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
        productRepository.save(product);
        return product;
    }
    
    public Product updateProduct(Long id, ProductInput input) {
        Product existing = productRepository.findById(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }
        
        existing.setName(input.getName());
        existing.setDescription(input.getDescription());
        existing.setPrice(input.getPrice());
        existing.setCategory(input.getCategory());
        existing.setStockQuantity(input.getStockQuantity());

        productRepository.save(existing);
        return existing;
    }
    
    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
    
    public int getProductCount() {
        return productRepository.count();
    }
    
    public Double getAveragePrice() {
        return productRepository.findAll().stream()
            .mapToDouble(Product::getPrice)
            .average()
            .orElse(0.0);
    }
    
    public List<String> getAllCategories() {
        return productRepository.findAll().stream()
            .map(Product::getCategory)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}