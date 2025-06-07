package io.microprofile.tutorial.store.product.resource;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/products")
@Tag(name = "Product Resource", description = "CRUD operations for products")
public class ProductResource {

    private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());
    private List<Product> products = new ArrayList<>();

    public ProductResource() {
        // Initialize the list with some sample products
        products.add(new Product(1L, "iPhone", "Apple iPhone 15", 999.99));
        products.add(new Product(2L, "MacBook", "Apple MacBook Air", 1299.0));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all products", description = "Returns a list of all products")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of products", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    })
    public Response getAllProducts() {
        LOGGER.info("Fetching all products");
        return Response.ok(products).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get product by ID", description = "Returns a product by its ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response getProductById(@PathParam("id") Long id) {
        LOGGER.info("Fetching product with id: " + id);
        Optional<Product> product = products.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (product.isPresent()) {
            return Response.ok(product.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new product", description = "Creates a new product")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Product created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    })
    public Response createProduct(Product product) {
        LOGGER.info("Creating product: " + product);
        products.add(product);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a product", description = "Updates an existing product by its ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response updateProduct(@PathParam("id") Long id, Product updatedProduct) {
        LOGGER.info("Updating product with id: " + id);
        for (Product product : products) {
            if (product.getId().equals(id)) {
                product.setName(updatedProduct.getName());
                product.setDescription(updatedProduct.getDescription());
                product.setPrice(updatedProduct.getPrice());
                return Response.ok(product).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Product deleted"),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response deleteProduct(@PathParam("id") Long id) {
        LOGGER.info("Deleting product with id: " + id);
        Optional<Product> product = products.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (product.isPresent()) {
            products.remove(product.get());
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}