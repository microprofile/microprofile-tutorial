package io.microprofile.tutorial.store.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

import io.microprofile.tutorial.store.product.repository.ProductRepository;
import io.microprofile.tutorial.store.product.cache.ProductCache;
import io.microprofile.tutorial.store.product.entity.Product;

@RequestScoped
public class ProductService {
    
    @Inject
    ProductRepository productRepository;

    @Inject
    private ProductCache productCache;

    /**
     * Retrieves a list of products. If the operation takes longer than 2 seconds,
     * fallback to cached data.
     */
    @Timeout(2000) // Set timeout to 2 seconds
    @Fallback(fallbackMethod = "getProductsFromCache") // Fallback method
    public List<Product> getProducts() {
        if (Math.random() > 0.7) {
            throw new RuntimeException("Simulated service failure");
        }
        return productRepository.findAllProducts();
    }

    /**
     * Fallback method to retrieve products from the cache.
     */
    public List<Product> getProductsFromCache() {
        System.out.println("Fetching products from cache...");
        return productCache.getAll().stream()
                .map(obj -> (Product) obj)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(
        requestVolumeThreshold = 10,
        failureRatio = 0.5,
        delay = 5000,
        successThreshold = 2,
        failOn = RuntimeException.class
    )
    @Fallback(FallbackHandlerImpl.class)
    public Product getProduct(Long id) {
        // Logic to call the product details service
        if (Math.random() > 0.7) {
            throw new RuntimeException("Simulated service failure");
        }

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