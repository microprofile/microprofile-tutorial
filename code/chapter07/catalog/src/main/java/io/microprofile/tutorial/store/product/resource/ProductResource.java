package io.microprofile.tutorial.store.product.resource;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.service.ProductService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.RegistryScope;

import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.List;

@Path("/products")
@ApplicationScoped
public class ProductResource {

    @Inject 
    @ConfigProperty(name = "product.isMaintenanceMode", defaultValue = "false")
    private boolean maintenanceMode;
    
    @Inject
    @RegistryScope(scope=MetricRegistry.APPLICATION_SCOPE)
    MetricRegistry metricRegistry;

    @Inject
    private ProductService productService;

    private long productCatalogSize;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all products", description = "Retrieves a list of all available products")
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successful, list of products found",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "400",
            description = "Unsuccessful, no products found",
            content = @Content(mediaType = "application/json")
        )
    })
    // Expose the response time as a timer metric
    @Timed(name = "productListFetchingTime",
            tags = {"method=getProducts"},
            absolute = true, 
            description = "Time needed to fetch the list of products")
    // Expose the invocation count as a counter metric
    @Counted(name = "productAccessCount",
            absolute = true,
            description = "Number of times the list of products is requested")
    public Response getProducts() {

        if (maintenanceMode) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service is currently in maintenance mode.")
                    .build();
        } 
        
        List<Product> products = productService.getProducts();
        productCatalogSize = products.size();
        if (products != null && !products.isEmpty()) {
            return Response
                    .status(Response.Status.OK)
                    .entity(products).build();
        } else {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("No products found")
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a product by ID", description = "Retrieves a single product by its ID")
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successful, product found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Product.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Unsuccessful, product not found",
            content = @Content(mediaType = "application/json")
        )
    })
    @Timed(name = "productLookupTime",
            tags = {"method=getProduct"},
            absolute = true, 
            description = "Time needed to lookup for a products")
    public Product getProduct(@PathParam("id") Long productId) {
        return productService.getProduct(productId);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided information")
    @APIResponse(
        responseCode = "201",
        description = "Successful, new product created",
        content = @Content(mediaType = "application/json")
    )
    @Timed(name = "productCreationTime",
    absolute = true,
    description = "Time needed to create a new product in the catalog")
    public Response createProduct(Product product) {
        productService.createProduct(product);
        return Response.status(Response.Status.CREATED).entity("New product created").build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a product", description = "Updates an existing product with the provided information")
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successful, product updated",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "404",
            description = "Unsuccessful, product not found",
            content = @Content(mediaType = "application/json")
        )
    })
    @Timed(name = "productModificationTime",
    absolute = true,
    description = "Time needed to modify an existing product in the catalog")
    public Response updateProduct(Product product) {
        Product updatedProduct = productService.updateProduct(product);
        if (updatedProduct != null) {
            return Response.status(Response.Status.OK).entity("Product updated").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
        }
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a product", description = "Deletes a product with the specified ID")
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successful, product deleted",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "404",
            description = "Unsuccessful, product not found",
            content = @Content(mediaType = "application/json")
        )
    })
    @Timed(name = "productDeletionTime",
    absolute = true,
    description = "Time needed to delete a product from the catalog")
    public Response deleteProduct(@PathParam("id") Long id) {
        
        Product product = productService.getProduct(id);
        if (product != null) {
            productService.deleteProduct(id);
            return Response.status(Response.Status.OK).entity("Product deleted").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
        }
    }

    @Gauge(name = "productCatalogSize", unit = "items", description = "Current number of products in the catalog")
    public long getProductCount() {
        return productCatalogSize;
    }
}