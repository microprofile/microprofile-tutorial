package io.microprofile.tutorial.graphql.product.entity;

import org.eclipse.microprofile.graphql.Type;
import org.eclipse.microprofile.graphql.Description;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review entity for product reviews
 */
@Type("Review")
@Description("A customer review for a product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview implements Identifiable {
    
    @Description("Unique review identifier")
    private Long id;
    
    @Description("Product ID this review belongs to")
    private Long productId;
    
    @Description("Reviewer name")
    private String reviewerName;
    
    @Description("Rating from 1 to 5")
    private Integer rating;
    
    @Description("Review comment")
    private String comment;
    
    @Description("Review creation date")
    private LocalDateTime createdAt;
}