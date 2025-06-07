package io.microprofile.tutorial.store.product.resource;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/products")
@Tag(name = "Product Resource", description = "CRUD operations for products")
public class ProductResource {

    private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

    @Inject
    @ConfigProperty(name="product.maintenanceMode", defaultValue="false")
    private boolean maintenanceMode;

    @Inject
    private ProductService productService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all products", description = "Retrieves a list of all products")
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Successful, list of products found", 
            content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Product.class))), 
        @APIResponse(
            responseCode = "400",
            description = "Unsuccessful, no products found",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "503",
            description = "Service is under maintenance",
            content = @Content(mediaType = "application/json")
        )
    })
    public Response getAllProducts() {
        LOGGER.log(Level.INFO, "REST: Fetching all products");
        List<Product> products = productService.findAllProducts();

        if (maintenanceMode) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service is under maintenance")
                    .build();
        }

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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get product by ID", description = "Returns a product by its ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
        @APIResponse(responseCode = "404", description = "Product not found"),
        @APIResponse(responseCode = "503", description = "Service is under maintenance")
    })
    public Response getProductById(@PathParam("id") Long id) {
        LOGGER.log(Level.INFO, "REST: Fetching product with id: {0}", id);

        if (maintenanceMode) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service is under maintenance")
                    .build();
        }

        Product product = productService.findProductById(id);
        if (product != null) {
            return Response.ok(product).build();
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
        LOGGER.info("REST: Creating product: " + product);
        Product createdProduct = productService.createProduct(product);
        return Response.status(Response.Status.CREATED).entity(createdProduct).build();
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
        LOGGER.info("REST: Updating product with id: " + id);
        Product updated = productService.updateProduct(id, updatedProduct);
        if (updated != null) {
            return Response.ok(updated).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
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
        LOGGER.info("REST: Deleting product with id: " + id);
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search products", description = "Search products by criteria")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search results", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    })
    public Response searchProducts(
            @QueryParam("name") String name,
            @QueryParam("description") String description,
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice) {
        LOGGER.info("REST: Searching products with criteria");
        List<Product> results = productService.searchProducts(name, description, minPrice, maxPrice);
        return Response.ok(results).build();
    }
}