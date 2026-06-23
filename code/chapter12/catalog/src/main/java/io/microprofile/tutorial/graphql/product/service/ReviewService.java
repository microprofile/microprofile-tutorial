package io.microprofile.tutorial.graphql.product.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.microprofile.tutorial.graphql.product.entity.ProductReview;
import io.microprofile.tutorial.graphql.product.repository.ReviewRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for review operations
 */
@ApplicationScoped
public class ReviewService {

    @Inject
    ReviewRepository reviewRepository;
    
    public List<ProductReview> findByProductId(Long productId) {
        return reviewRepository.findAll().stream()
            .filter(r -> r.getProductId().equals(productId))
            .collect(Collectors.toList());
    }
    
    public List<ProductReview> findByProductIds(List<Long> productIds) {
        Set<Long> productIdSet = new HashSet<>(productIds);
        return reviewRepository.findAll().stream()
            .filter(r -> productIdSet.contains(r.getProductId()))
            .collect(Collectors.toList());
    }
    
    public List<ProductReview> findTopReviewsByProductId(Long productId, int limit) {
        return reviewRepository.findAll().stream()
            .filter(r -> r.getProductId().equals(productId))
            .sorted(Comparator.comparing(ProductReview::getRating).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public Double getAverageRating(Long productId) {
        return reviewRepository.findAll().stream()
            .filter(r -> r.getProductId().equals(productId))
            .mapToInt(ProductReview::getRating)
            .average()
            .orElse(0.0);
    }
    
    public List<ProductReview> getRecentReviews(int limit) {
        return reviewRepository.findAll().stream()
            .sorted(Comparator.comparing(ProductReview::getId).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public ProductReview findById(Long id) {
        return reviewRepository.findAll().stream()
            .filter(r -> r.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}