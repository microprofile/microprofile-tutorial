package io.microprofile.tutorial.graphql.product.api;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Source;
import org.eclipse.microprofile.graphql.DateFormat;
import io.microprofile.tutorial.graphql.product.dto.ProductInput;
import io.microprofile.tutorial.graphql.product.entity.Product;
import io.microprofile.tutorial.graphql.product.entity.ProductReview;
import io.microprofile.tutorial.graphql.product.entity.Identifiable;
import io.microprofile.tutorial.graphql.product.exception.ProductNotFoundException;
import io.microprofile.tutorial.graphql.product.exception.InsufficientStockException;
import io.microprofile.tutorial.graphql.product.service.ProductService;
import io.microprofile.tutorial.graphql.product.service.ReviewService;
import io.microprofile.tutorial.graphql.product.service.PricingService;
import io.microprofile.tutorial.graphql.product.service.InventoryService;
import io.microprofile.tutorial.graphql.product.service.OrderService;
import io.microprofile.tutorial.graphql.product.entity.Order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

/**
 * GraphQL API for product operations
 */
@GraphQLApi
@ApplicationScoped
@Description("Product management GraphQL API")
public class ProductGraphQLApi {
    
    @Inject
    @ConfigProperty(name = "product.max.results", defaultValue = "100")
    private Integer maxResults;
    
    @Inject
    @ConfigProperty(name = "product.currency", defaultValue = "USD")
    private String currency;
    
    @Inject
    ProductService productService;
    
    @Inject
    ReviewService reviewService;
    
    @Inject
    PricingService pricingService;
    
    @Inject
    InventoryService inventoryService;
    
    @Inject
    OrderService orderService;
    
    // ===== Queries =====
    
    @Query("products")
    @Description("Retrieves all products from the catalog")
    public List<Product> getAllProducts() {
        // Use maxResults to limit query results
        return productService.getProducts(maxResults);
    }
    
    @Query("product")
    @Description("Retrieves a single product by its unique identifier")
    public Product getProduct(
            @Name("id") 
            @Description("The unique identifier of the product") 
            Long id) {
        Product product = productService.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }
    
    @Query("searchProducts")
    @Description("Search for products by name or category")
    public List<Product> searchProducts(
            @Name("searchTerm") 
            @Description("Search term to match against product name or description")
            String searchTerm,
            @Name("category") 
            @Description("Filter by category")
            String category) {
        return productService.search(searchTerm, category);
    }
    
    @Query("productCount")
    @Description("Returns the total number of products in the catalog")
    public int getProductCount() {
        return productService.getProductCount();
    }
    
    @Query("averagePrice")
    @Description("Returns the average price of all products")
    public Double getAveragePrice() {
        return productService.getAveragePrice();
    }
    
    @Query("testRuntimeException")
    @Description("Demonstrates runtime exception handling - throws RuntimeException for testing")
    public String testRuntimeException() {
        throw new RuntimeException("This is a test runtime exception that will be caught and converted to a GraphQL error");
    }
    
    @Query("categories")
    @Description("Returns all available product categories")
    public List<String> getCategories() {
        return productService.getAllCategories();
    }
    
    @Query("productReleaseDate")
    @Description("Returns the release date of a product in formatted string")
    @DateFormat(value = "dd MMM yyyy")
    public LocalDate getProductReleaseDate(@Name("id") Long id) {
        Product product = productService.findById(id);
        return product != null ? product.getReleaseDate() : null;
    }
    
    @Query("recentItems")
    @Description("Returns recently added products and reviews as a unified list. " +
                 "Demonstrates polymorphic queries using the Identifiable interface.")
    public List<Identifiable> getRecentItems(
            @Name("limit") 
            @DefaultValue("10")
            @Description("Maximum number of items to return")
            int limit) {
        // Get recent products and reviews, then combine and sort by ID (newest first)
        List<Identifiable> items = new java.util.ArrayList<>();
        
        // Add recent products
        List<Product> recentProducts = productService.getProducts(limit / 2);
        items.addAll(recentProducts);
        
        // Add recent reviews
        List<ProductReview> recentReviews = reviewService.getRecentReviews(limit / 2);
        items.addAll(recentReviews);
        
        // Sort by ID descending (assuming higher IDs are newer)
        items.sort((a, b) -> Long.compare(b.getId(), a.getId()));
        
        // Return top 'limit' items
        return items.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    @Query("findById")
    @Description("Find any entity (Product or Review) by its ID. " +
                 "Demonstrates the benefits of the Identifiable interface for unified lookups.")
    public Identifiable findById(
            @Name("id")
            @Description("The unique identifier to search for")
            Long id) {
        // Try to find as Product first
        Product product = productService.findById(id);
        if (product != null) {
            return product;
        }
        
        // Try to find as Review
        ProductReview review = reviewService.findById(id);
        if (review != null) {
            return review;
        }
        
        return null; // Not found
    }
    
    // ===== Mutations =====
    
    @Mutation("createProduct")
    @Description("Creates a new product in the catalog")
    public Product createProduct(
            @Name("input") 
            @Description("Product input data") 
            ProductInput input) {
        return productService.createProduct(input);
    }
    
    @Mutation("updateProduct")
    @Description("Updates an existing product")
    public Product updateProduct(
            @Name("id") 
            @Description("Product ID to update") 
            Long id,
            @Name("input") 
            @Description("Updated product data") 
            ProductInput input) {
        return productService.updateProduct(id, input);
    }
    
    @Mutation("deleteProduct")
    @Description("Deletes a product from the catalog")
    public boolean deleteProduct(
            @Name("id") 
            @Description("Product ID to delete") 
            Long id) {
        return productService.deleteProduct(id);
    }
    
    @Mutation("orderProduct")
    @Description("Place an order for a product - demonstrates custom GraphQL exception handling")
    public Order orderProduct(
            @Name("productId")
            @Description("Product ID to order")
            Long productId,
            @Name("quantity")
            @Description("Quantity to order")
            int quantity) throws InsufficientStockException {
        int available = inventoryService.getStockLevel(productId);
        if (available < quantity) {
            throw new InsufficientStockException(productId, quantity, available);
        }
        return orderService.createOrder(productId, quantity);
    }
    
    // ===== Field Resolvers =====
    
    /**
     * Field resolver to add reviews to a product
     * Non-batched version - causes N+1 queries
     */
    @Description("Reviews for this product")
    public List<ProductReview> reviews(@Source Product product) {
        return reviewService.findByProductId(product.getId());
    }
    
    /**
     * Field resolver with parameters
     */
    @Description("Top reviews for this product")
    public List<ProductReview> topReviews(
            @Source Product product,
            @Name("limit") 
            @DefaultValue("5") 
            @Description("Maximum number of reviews to return")
            int limit) {
        return reviewService.findTopReviewsByProductId(product.getId(), limit);
    }
    
    /**
     * Computed field - average rating
     */
    @Description("Average rating for this product")
    public Double averageRating(@Source Product product) {
        return reviewService.getAverageRating(product.getId());
    }
    
    /**
     * Computed field - price category
     */
    @Description("Price category based on product price")
    public String priceCategory(@Source Product product) {
        Double price = product.getPrice();
        if (price < 50) return "BUDGET";
        if (price < 200) return "STANDARD";
        if (price < 500) return "PREMIUM";
        return "LUXURY";
    }
    
    /**
     * Computed field using external service - calculate discounted price
     */
    @Description("Calculate discounted price using a discount code")
    public Double discountedPrice(
            @Source Product product,
            @Name("discountCode") 
            @Description("Discount code to apply (e.g., SAVE10, SAVE20, SAVE30, HALF)")
            String discountCode) {
        return pricingService.calculateDiscountedPrice(product.getPrice(), discountCode);
    }
    
    /**
     * Field resolver - adds 'stockLevel' field to Product type
     * Demonstrates @Source annotation for adding fields from external service
     */
    @Description("Current stock level from inventory system")
    public int stockLevel(@Source Product product) {
        return inventoryService.getStockLevel(product.getId());
    }
    
    /**
     * Field resolver that may fail for some products
     * Demonstrates partial results when some fields throw exceptions
     */
    @Description("Special promotional price (may not be available for all products)")
    public Double specialPrice(@Source Product product) throws org.eclipse.microprofile.graphql.GraphQLException {
        try {
            // This may throw exception for certain products
            pricingService.calculateSpecialPrice(product.getId());
            // If successful, return 10% discount
            return product.getPrice() * 0.9;
        } catch (Exception e) {
            throw new org.eclipse.microprofile.graphql.GraphQLException(
                "Failed to calculate special price",
                org.eclipse.microprofile.graphql.GraphQLException.ExceptionType.DataFetchingException);
        }
    }
}
