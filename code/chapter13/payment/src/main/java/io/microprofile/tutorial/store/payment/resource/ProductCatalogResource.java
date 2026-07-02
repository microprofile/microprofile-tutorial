package io.microprofile.tutorial.store.payment.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.microprofile.tutorial.store.payment.dto.product.Product;
import io.microprofile.tutorial.store.payment.service.ProductCatalogService;
import io.microprofile.tutorial.store.payment.exception.ProductNotFoundException;
import io.microprofile.tutorial.store.payment.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * REST resource demonstrating MicroProfile Rest Client usage.
 * 
 * This resource showcases:
 * - CDI injection of service that uses REST client
 * - REST endpoints that proxy calls to remote services
 * - Error handling and response mapping
 * - OpenAPI documentation
 */
@ApplicationScoped
@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product Catalog", description = "REST Client demonstration endpoints")
public class ProductCatalogResource {
    
    private static final Logger LOGGER = Logger.getLogger(ProductCatalogResource.class.getName());

    @Inject
    ProductCatalogService catalogService;

    @Inject
    io.microprofile.tutorial.store.payment.service.ProductClientBuilderService builderService;

    @Inject
    io.microprofile.tutorial.store.payment.service.FilteredProductCatalogService filteredCatalogService;

    /**
     * Gets all products using MicroProfile Rest Client.
     * 
     * Demonstrates:
     * - Simple GET request through REST client
     * - Automatic JSON serialization
     * - List response handling
     */
    @GET
   @Path("/products")
    @Operation(summary = "Get all products", 
               description = "Fetches all products from the catalog service using MP Rest Client")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Products retrieved successfully"),
        @APIResponse(responseCode = "503", description = "Catalog service unavailable")
    })
    public Response getAllProducts() {
        LOGGER.info("REST: Fetching all products via MicroProfile Rest Client");
        try {
            List<Product> products = catalogService.getAllProducts();
            return Response.ok(products).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to fetch products: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("error", "Catalog service unavailable", "message", e.getMessage()))
                .build();
        }
    }

    /**
     * Gets a specific product by ID.
     * 
     * Demonstrates:
     * - Path parameter usage
     * - Single entity response
     * - 404 handling
     */
    @GET
    @Path("/products/{id}")
    @Operation(summary = "Get product by ID", 
               description = "Fetches a specific product using MP Rest Client with @PathParam")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product found"),
        @APIResponse(responseCode = "404", description = "Product not found"),
        @APIResponse(responseCode = "503", description = "Catalog service unavailable")
    })
    public Response getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("REST: Fetching product " + id + " via MicroProfile Rest Client");
        try {
            Product product = catalogService.getProduct(id);
            if (product != null) {
                return Response.ok(product).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Product not found", "productId", id))
                    .build();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to fetch product: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("error", "Catalog service unavailable", "message", e.getMessage()))
                .build();
        }
    }

    /**
     * Checks if a product is available for purchase.
     * 
     * Demonstrates:
     * - Boolean logic with REST client
     * - Business validation
     */
    @GET
    @Path("/products/{id}/availability")
    @Operation(summary = "Check product availability", 
               description = "Validates if a product is available for payment processing")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Availability check completed"),
        @APIResponse(responseCode = "503", description = "Catalog service unavailable")
    })
    public Response checkProductAvailability(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("REST: Checking availability for product " + id);
        try {
            boolean available = catalogService.isProductAvailable(id);
            return Response.ok(Map.of(
                "productId", id,
                "available", available,
                "message", available ? "Product is available" : "Product is not available"
            )).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to check availability: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("error", "Catalog service unavailable", "message", e.getMessage()))
                .build();
        }
    }

    /**
     * Validates product price for payment processing.
     * 
     * Demonstrates:
     * - Query parameter usage
     * - Price validation logic
     */
    @GET
    @Path("/products/{id}/validate-price")
    @Operation(summary = "Validate product price", 
               description = "Validates if the product price matches expected amount")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Price validation completed"),
        @APIResponse(responseCode = "400", description = "Invalid input"),
        @APIResponse(responseCode = "503", description = "Catalog service unavailable")
    })
    public Response validateProductPrice(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id,
            @Parameter(description = "Expected price", required = true)
            @QueryParam("price") Double price) {
        
        if (price == null || price <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid price", "message", "Price must be greater than 0"))
                .build();
        }
        
        LOGGER.info(String.format("REST: Validating price for product %d (expected: %.2f)", id, price));
        try {
            boolean valid = catalogService.validateProductPrice(id, price);
            return Response.ok(Map.of(
                "productId", id,
                "expectedPrice", price,
                "valid", valid,
                "message", valid ? "Price matches" : "Price mismatch detected"
            )).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to validate price: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("error", "Catalog service unavailable", "message", e.getMessage()))
                .build();
        }
    }

    /**
     * Demonstrates exception handling with ResponseExceptionMapper.
     * 
     * This endpoint shows how different HTTP error codes are mapped to exceptions:
     * - 404 → ProductNotFoundException (checked exception)
     * - 503 → ServiceUnavailableException (unchecked exception)
     * - Other errors → RuntimeException
     * 
     * Try with different IDs to see error handling:
     * - Valid ID (e.g., 1) → Returns product
     * - Invalid ID (e.g., 999999) → 404 ProductNotFoundException
     */
    @GET
    @Path("/products/{id}/detailed")
    @Operation(summary = "Get product with detailed error handling", 
               description = "Demonstrates ResponseExceptionMapper with custom exception handling")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product found"),
        @APIResponse(responseCode = "404", description = "Product not found (handled by ResponseExceptionMapper)"),
        @APIResponse(responseCode = "503", description = "Service unavailable (handled by ResponseExceptionMapper)")
    })
    public Response getProductWithExceptionHandling(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("REST: Demonstrating exception handling for product " + id);
        try {
            Product product = catalogService.getProduct(id);
            
            if (product != null) {
                return Response.ok(Map.of(
                    "productId", id,
                    "product", product,
                    "note", "Product retrieved successfully - no exceptions thrown"
                )).build();
            } else {
                // Product not found (404 was handled by ResponseExceptionMapper)
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                        "productId", id,
                        "error", "Product not found",
                        "note", "ProductNotFoundException was caught and handled by the service layer"
                    ))
                    .build();
            }
            
        } catch (ServiceUnavailableException e) {
            // Unchecked exception from ResponseExceptionMapper
            LOGGER.severe("Catalog service unavailable: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of(
                    "productId", id,
                    "error", "Catalog service unavailable",
                    "statusCode", e.getStatusCode(),
                    "message", e.getMessage(),
                    "note", "ServiceUnavailableException (unchecked) was thrown by ResponseExceptionMapper"
                ))
                .build();
                
        } catch (RuntimeException e) {
            // Other runtime exceptions from ResponseExceptionMapper
            LOGGER.severe("Unexpected error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "error", "Unexpected error",
                    "message", e.getMessage(),
                    "note", "RuntimeException was thrown by ResponseExceptionMapper"
                ))
                .build();
        }
    }

    // ====================================================================================
    // RestClientBuilder (Programmatic Client Creation) Examples
    // ====================================================================================

    /**
     * Checks product availability using RestClientBuilder (programmatic creation).
     * 
     * Demonstrates:
     * - Creating REST client programmatically with RestClientBuilder
     * - Try-with-resources pattern for automatic resource cleanup
     * - Using baseUri(String) method (MicroProfile Rest Client 4.0)
     * - Configuring timeouts programmatically
     * - When to prefer programmatic over CDI injection
     * 
     * Example: GET /catalog/builder/products/1/check
     * 
     * @param id Product ID to check
     * @return JSON response indicating if product is available
     */
    @GET
    @Path("/builder/products/{id}/check")
    @Operation(
        summary = "Check product availability (RestClientBuilder)",
        description = "Uses RestClientBuilder to programmatically create a REST client and check product availability. " +
                     "Demonstrates try-with-resources pattern and dynamic client configuration."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Product availability check completed"
        )
    })
    public Response checkProductWithBuilder(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("Checking product availability using RestClientBuilder: " + id);
        
        try {
            boolean available = builderService.isProductAvailable(id);
            return Response.ok(Map.of(
                "productId", id,
                "available", available,
                "method", "RestClientBuilder (programmatic)",
                "note", available 
                    ? "Product found using programmatically created client"
                    : "Product not found - ProductNotFoundException caught"
            )).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error checking product: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "error", "Error checking product",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Gets all products using RestClientBuilder.
     * 
     * Demonstrates:
     * - Programmatic client creation for simple GET requests
     * - Automatic resource cleanup with AutoCloseable interface
     * - Comparison with CDI-injected client approach
     * 
     * Example: GET /catalog/builder/products
     * 
     * @return List of products retrieved using programmatic client
     */
    @GET
    @Path("/builder/products")
    @Operation(
        summary = "Get all products (RestClientBuilder)",
        description = "Retrieves all products using a programmatically created REST client. " +
                     "Compare this with the CDI-injected version at /catalog/products"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Products retrieved successfully"
        )
    })
    public Response getAllProductsWithBuilder() {
        LOGGER.info("Getting all products using RestClientBuilder");
        
        try {
            List<Product> products = builderService.getAllProducts();
            return Response.ok(Map.of(
                "products", products,
                "count", products.size(),
                "method", "RestClientBuilder (programmatic)",
                "note", "Client created with RestClientBuilder.newBuilder().baseUri(...).build()"
            )).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error getting products: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Error retrieving products",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Gets product with dynamic configuration from MicroProfile Config.
     * 
     * Demonstrates:
     * - Reading configuration at runtime with ConfigProvider
     * - Dynamic client configuration based on external properties
     * - Flexibility of programmatic client creation
     * 
     * Example: GET /catalog/builder/products/1/dynamic
     * 
     * @param id Product ID
     * @return Product retrieved with dynamically configured client
     */
    @GET
    @Path("/builder/products/{id}/dynamic")
    @Operation(
        summary = "Get product with dynamic config (RestClientBuilder)",
        description = "Demonstrates using RestClientBuilder with configuration loaded from MicroProfile Config at runtime. " +
                     "Shows how to read properties like base URL and timeouts dynamically."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Product retrieved successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public Response getProductWithDynamicConfig(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("Getting product with dynamic configuration: " + id);
        
        try {
            Product product = builderService.getProductWithDynamicConfig(id);
            
            if (product != null) {
                return Response.ok(Map.of(
                    "product", product,
                    "method", "RestClientBuilder with dynamic config",
                    "note", "Configuration loaded from microprofile-config.properties at runtime"
                )).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                        "productId", id,
                        "error", "Product not found",
                        "method", "RestClientBuilder with dynamic config"
                    ))
                    .build();
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error getting product: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "error", "Error retrieving product",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Gets product for specific environment.
     * 
     * Demonstrates:
     * - Environment-specific client configuration
     * - Conditional configuration based on deployment environment
     * - Different timeouts for different environments
     * 
     * Example: GET /catalog/builder/products/1/env/dev
     * 
     * @param environment Environment name (dev, staging, prod)
     * @param id Product ID
     * @return Product retrieved with environment-specific configuration
     */
    @GET
    @Path("/builder/products/{id}/env/{environment}")
    @Operation(
        summary = "Get product for environment (RestClientBuilder)",
        description = "Demonstrates environment-specific REST client configuration. " +
                     "Different base URLs and timeouts are used based on the environment parameter."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Product retrieved successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public Response getProductForEnvironment(
            @Parameter(description = "Environment (dev, staging, prod)", required = true)
            @PathParam("environment") String environment,
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info(String.format("Getting product for environment %s: %s", environment, id));
        
        try {
            Product product = builderService.getProductForEnvironment(environment, id);
            
            if (product != null) {
                return Response.ok(Map.of(
                    "product", product,
                    "environment", environment,
                    "method", "RestClientBuilder with environment-specific config",
                    "note", String.format("Client configured for %s environment", environment)
                )).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                        "productId", id,
                        "environment", environment,
                        "error", "Product not found"
                    ))
                    .build();
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error getting product: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "environment", environment,
                    "error", "Error retrieving product",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Checks availability of multiple products in batch.
     * 
     * Demonstrates:
     * - Creating multiple clients with different configurations
     * - When programmatic creation is preferred for batch operations
     * - Handling multiple requests efficiently
     * - Shorter timeouts for batch processing
     * 
     * Example: POST /catalog/builder/products/batch-check
     * Body: {"productIds": [1, 2, 3, 4, 5]}
     * 
     * @param request Request containing list of product IDs
     * @return Batch check results
     */
    @POST
    @Path("/builder/products/batch-check")
    @Operation(
        summary = "Batch check product availability (RestClientBuilder)",
        description = "Checks availability of multiple products using programmatically created clients. " +
                     "Demonstrates when programmatic creation is preferred over CDI injection."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Batch check completed"
        )
    })
    public Response batchCheckProducts(Map<String, List<Long>> request) {
        List<Long> productIds = request.get("productIds");
        
        if (productIds == null || productIds.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Missing or empty productIds array",
                    "example", Map.of("productIds", List.of(1, 2, 3))
                ))
                .build();
        }
        
        LOGGER.info("Batch checking " + productIds.size() + " products");
        
        try {
            int availableCount = builderService.checkMultipleProducts(productIds);
            
            return Response.ok(Map.of(
                "totalChecked", productIds.size(),
                "availableCount", availableCount,
                "unavailableCount", productIds.size() - availableCount,
                "method", "RestClientBuilder (batch processing)",
                "note", "Each product checked with a separate programmatically created client instance"
            )).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error in batch check: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Error in batch check",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    // ====================================================================================
    // Custom Filters and Interceptors Examples
    // ====================================================================================

    /**
     * Gets all products using REST client with custom filters.
     * 
     * Demonstrates:
     * - ClientRequestFilter for authentication (BearerTokenFilter)
     * - ClientRequestFilter for correlation tracking (CorrelationIdFilter)
     * - ClientRequestFilter for request logging (RequestLoggingFilter)
     * - ClientResponseFilter for response logging (ResponseLoggingFilter)
     * - Filter execution order using @Priority
     * 
     * Compare the server logs for this endpoint with /catalog/products to see
     * the comprehensive logging and tracing provided by the filters.
     * 
     * Example: GET /catalog/filtered/products
     * 
     * @return List of products with full filter logging
     */
    @GET
    @Path("/filtered/products")
    @Operation(
        summary = "Get all products with filters",
        description = "Retrieves products using a REST client with custom filters registered. " +
                     "Check server logs to see filter execution: BearerTokenFilter (auth), " +
                     "CorrelationIdFilter (tracing), RequestLoggingFilter and ResponseLoggingFilter."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Products retrieved with comprehensive filter logging"
        )
    })
    public Response getAllProductsWithFilters() {
        LOGGER.info("Getting all products with custom filters");
        
        try {
            List<Product> products = filteredCatalogService.getAllProducts();
            return Response.ok(Map.of(
                "products", products,
                "count", products.size(),
                "method", "REST Client with Custom Filters",
                "filters", List.of(
                    "BearerTokenFilter (Priority 1000 - AUTHENTICATION)",
                    "CorrelationIdFilter (Priority 100)",
                    "RequestLoggingFilter (Priority 300)",
                    "ResponseLoggingFilter (Priority 300)"
                ),
                "note", "Check server logs for detailed filter execution traces"
            )).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error getting products with filters: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Error retrieving products",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Gets a specific product using REST client with custom filters.
     * 
     * Demonstrates:
     * - Filter execution with path parameters
     * - Correlation ID propagation
     * - Request/response logging for specific resource retrieval
     * - Authentication header injection
     * 
     * Example: GET /catalog/filtered/products/1
     * 
     * @param id Product ID
     * @return Product with comprehensive filter logging
     */
    @GET
    @Path("/filtered/products/{id}")
    @Operation(
        summary = "Get product by ID with filters",
        description = "Retrieves a specific product using filtered REST client. " +
                     "Demonstrates filter execution with path parameters and exception handling."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Product retrieved successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public Response getProductWithFilters(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("Getting product with custom filters: " + id);
        
        try {
            Product product = filteredCatalogService.getProduct(id);
            return Response.ok(Map.of(
                "product", product,
                "method", "REST Client with Custom Filters",
                "filterChain", "BearerToken → CorrelationId → RequestLogging → [HTTP] → ResponseLogging",
                "note", "Check server logs for X-Correlation-ID and detailed request/response traces"
            )).build();
            
        } catch (ProductNotFoundException e) {
            LOGGER.warning("Product not found with filters: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                    "productId", id,
                    "error", "Product not found",
                    "note", "404 response was logged by ResponseLoggingFilter before exception was thrown"
                ))
                .build();
                
        } catch (Exception e) {
            LOGGER.severe("Error getting product with filters: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "error", "Error retrieving product",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Checks product availability using filtered REST client.
     * 
     * Demonstrates:
     * - How filters handle error responses (404)
     * - Response logging includes error status codes
     * - Correlation IDs help trace failed requests
     * 
     * Example: GET /catalog/filtered/products/999999/available
     * 
     * @param id Product ID to check
     * @return Availability status with filter logging
     */
    @GET
    @Path("/filtered/products/{id}/available")
    @Operation(
        summary = "Check product availability with filters",
        description = "Checks if a product is available using filtered REST client. " +
                     "Demonstrates how filters log both successful and error responses."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Availability check completed"
        )
    })
    public Response checkAvailabilityWithFilters(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("Checking availability with filters: " + id);
        
        try {
            boolean available = filteredCatalogService.isProductAvailable(id);
            return Response.ok(Map.of(
                "productId", id,
                "available", available,
                "method", "REST Client with Custom Filters",
                "note", available 
                    ? "Product found - check logs for filter execution" 
                    : "Product not found - ResponseLoggingFilter logged 404 before returning false"
            )).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error checking availability with filters: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "error", "Error checking availability",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Comparison endpoint showing differences between filtered and non-filtered clients.
     * 
     * Demonstrates:
     * - Side-by-side comparison of filtered vs non-filtered REST clients
     * - Impact of filters on observability
     * - When to use filters vs. plain clients
     * 
     * Example: GET /catalog/compare/1
     * 
     * @param id Product ID to retrieve with both clients
     * @return Comparison results
     */
    @GET
    @Path("/compare/{id}")
    @Operation(
        summary = "Compare filtered vs non-filtered clients",
        description = "Retrieves the same product using both filtered and non-filtered clients. " +
                     "Check server logs to compare the logging output."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Comparison completed"
        )
    })
    public Response compareClients(
            @Parameter(description = "Product ID", required = true)
            @PathParam("id") Long id) {
        
        LOGGER.info("========== Starting Client Comparison ==========");
        
        try {
            // Call with non-filtered client
            LOGGER.info("--- Calling NON-FILTERED client ---");
            Product productNoFilter = catalogService.getProduct(id);
            
            // Small delay for log clarity
            Thread.sleep(100);
            
            // Call with filtered client
            LOGGER.info("--- Calling FILTERED client ---");
            Product productWithFilter = filteredCatalogService.getProduct(id);
            
            LOGGER.info("========== Client Comparison Complete ==========");
            
            return Response.ok(Map.of(
                "productId", id,
                "productName", productNoFilter.getName(),
                "comparison", Map.of(
                    "noFilters", "Minimal logging - only business logic logs",
                    "withFilters", "Comprehensive logging - authentication, tracing, request/response details"
                ),
                "filterBenefits", List.of(
                    "Authentication without changing business code",
                    "Distributed tracing with correlation IDs",
                    "Complete request/response visibility",
                    "Easier debugging and monitoring",
                    "Cross-cutting concerns separated from business logic"
                ),
                "note", "Review server logs to see the dramatic difference in observability"
            )).build();
            
        } catch (ProductNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                    "productId", id,
                    "error", "Product not found"
                ))
                .build();
                
        } catch (Exception e) {
            LOGGER.severe("Error in comparison: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "productId", id,
                    "error", "Error during comparison",
                    "message", e.getMessage()
                ))
                .build();
        }
    }
}
