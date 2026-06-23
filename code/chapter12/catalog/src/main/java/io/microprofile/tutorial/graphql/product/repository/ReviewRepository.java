package io.microprofile.tutorial.graphql.product.repository;

import jakarta.enterprise.context.ApplicationScoped;
import io.microprofile.tutorial.graphql.product.entity.ProductReview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repository layer for review persistence operations.
 */
@ApplicationScoped
public class ReviewRepository {

    private final Map<Long, ProductReview> reviews = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ReviewRepository() {
        initializeSampleData();
    }

    public List<ProductReview> findAll() {
        return new ArrayList<>(reviews.values());
    }

    public ProductReview save(ProductReview review) {
        reviews.put(review.getId(), review);
        return review;
    }

    public Long nextId() {
        return idCounter.getAndIncrement();
    }

    private void initializeSampleData() {
        seed(1L, "John Doe", 5, "Excellent laptop, very fast!");
        seed(1L, "Jane Smith", 4, "Good performance, a bit pricey.");
        seed(2L, "Bob Johnson", 5, "Perfect mouse, comfortable grip.");
        seed(3L, "Alice Brown", 4, "Great keyboard, keys are responsive.");
        seed(4L, "Charlie Davis", 5, "Amazing display quality!");
    }

    private void seed(Long productId, String reviewerName, Integer rating, String comment) {
        Long id = nextId();
        ProductReview review = new ProductReview(id, productId, reviewerName, rating, comment, LocalDateTime.now());
        save(review);
    }
}
