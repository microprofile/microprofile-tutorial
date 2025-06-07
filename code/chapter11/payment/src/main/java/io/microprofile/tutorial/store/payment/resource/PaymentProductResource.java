package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.client.ProductClientJson;
import io.microprofile.tutorial.store.payment.dto.product.Product;
import io.microprofile.tutorial.store.payment.service.ProductIntegrationService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * REST resource demonstrating how to use ProductClientJson.getProductsWithJsonp 
 * within REST endpoints for the Payment service.
 */
@ApplicationScoped
@Path("/products")
public class PaymentProductResource {
    
    private static final Logger LOGGER = Logger.getLogger(PaymentProductResource.class.getName());

    @Inject
    private ProductIntegrationService productService;

    @Inject
    @ConfigProperty(name = "catalog.service.url", defaultValue = "http://localhost:5050/catalog/api/products")
    private String catalogServiceUrl;

    /**
     * Gets all products available for payment processing.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all products", description = "Retrieves all products available for payment processing")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Products retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Failed to retrieve products")
    })
    public Response getAllProducts() {
        LOGGER.info("REST: Fetching all products for payment processing");
        
        try {
            Product[] products = ProductClientJson.getProductsWithJsonp(catalogServiceUrl);
            
            if (products != null) {
                LOGGER.info("Successfully retrieved " + products.length + " products");
                return Response.ok(products).build();
            } else {
                LOGGER.warning("No products returned from catalog service");
                return Response.ok(new Product[0]).build();
            }
            
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve products: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to retrieve products", "message", e.getMessage()))
                .build();
        }
    }

    /**
     * Gets products from a specific catalog service URL.
     */
    @GET
    @Path("/from-url")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get products from specific URL", description = "Retrieves products from a specified catalog service URL")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Products retrieved successfully"),
        @APIResponse(responseCode = "400", description = "Invalid URL provided"),
        @APIResponse(responseCode = "500", description = "Failed to retrieve products")
    })
    public Response getProductsFromUrl(
            @Parameter(description = "Catalog service URL", required = true)
            @QueryParam("url") String catalogUrl) {
        
        if (catalogUrl == null || catalogUrl.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "URL parameter is required"))
                .build();
        }

        LOGGER.info("REST: Fetching products from URL: " + catalogUrl);
        
        try {
            Product[] products = ProductClientJson.getProductsWithJsonp(catalogUrl);
            
            Map<String, Object> result = new HashMap<>();
            result.put("sourceUrl", catalogUrl);
            result.put("productCount", products != null ? products.length : 0);
            result.put("products", products != null ? products : new Product[0]);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve products from " + catalogUrl + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Failed to retrieve products", 
                    "url", catalogUrl,
                    "message", e.getMessage()))
                .build();
        }
    }

    /**
     * Validates if a product is available for payment processing.
     */
    @GET
    @Path("/{productId}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Validate product for payment", description = "Validates if a product is available for payment processing")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product validation completed"),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response validateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathParam("productId") Long productId) {
        
        LOGGER.info("REST: Validating product ID: " + productId);
        
        boolean isValid = productService.validateProductForPayment(productId);
        Product productDetails = productService.getProductDetails(productId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("isValid", isValid);
        result.put("availableForPayment", isValid);
        
        if (productDetails != null) {
            result.put("product", productDetails);
        }
        
        Response.Status status = isValid ? Response.Status.OK : Response.Status.NOT_FOUND;
        return Response.status(status).entity(result).build();
    }

    /**
     * Gets products within a specific price range.
     */
    @GET
    @Path("/price-range")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get products by price range", description = "Retrieves products within a specified price range")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Products retrieved successfully"),
        @APIResponse(responseCode = "400", description = "Invalid price range")
    })
    public Response getProductsByPriceRange(
            @Parameter(description = "Minimum price", required = true)
            @QueryParam("minPrice") @DefaultValue("0") double minPrice,
            @Parameter(description = "Maximum price", required = true)
            @QueryParam("maxPrice") @DefaultValue("1000") double maxPrice) {
        
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid price range. minPrice and maxPrice must be >= 0 and minPrice <= maxPrice"))
                .build();
        }

        LOGGER.info(String.format("REST: Fetching products in price range: $%.2f - $%.2f", minPrice, maxPrice));
        
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        
        Map<String, Object> result = new HashMap<>();
        result.put("minPrice", minPrice);
        result.put("maxPrice", maxPrice);
        result.put("productCount", products.size());
        result.put("products", products);
        
        return Response.ok(result).build();
    }
}
