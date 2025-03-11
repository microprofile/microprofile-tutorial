package io.microprofile.tutorial.store.product.resource;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.service.ProductService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.microprofile.tutorial.store.product.service.ProductService;

import java.util.List;

@Path("/products")
@ApplicationScoped
public class ProductResource {

    @Inject 
    @ConfigProperty(name = "product.isMaintenanceMode", defaultValue = "false")
    private boolean maintenanceMode;
    
    @Inject
    private ProductService productService;

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
    public Response getProducts() {

        if (maintenanceMode) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service is currently in maintenance mode.")
                    .build();
        } 
        
        List<Product> products = productService.findAllProducts();
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
    public Product getProduct(@PathParam("id") Long productId) {
        return productService.findProductById(productId);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided information")
    @APIResponse(
        responseCode = "201",
        description = "Successful, new product created",
        content = @Content(mediaType = "application/json")
    )
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
    public Response deleteProduct(@PathParam("id") Long id) {
        Product product = productService.findProductById(id);
        if (product != null) {
            productService.deleteProduct(product);
            return Response.status(Response.Status.OK).entity("Product deleted").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
        }
    }
}