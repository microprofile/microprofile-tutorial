package io.microprofile.tutorial.store.product.resource;

import java.util.List;

import io.microprofile.tutorial.store.logging.Loggable;
import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/products")
@ApplicationScoped  
public class ProductResource {

    @Inject
    private ProductRepository productRepository;

    @GET
    @Loggable
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Product getProduct(@PathParam("id") Long productId) {
        return productRepository.findProductById(productId);
    }

    @GET
    @Loggable
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<Product> getProducts() {
        // Return a list of products
        return productRepository.findAllProducts();
    }
    
    @POST
    @Loggable
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createProduct(Product product) {
        System.out.println("Creating product");
        productRepository.createProduct(product);
        return Response.status(Response.Status.CREATED)
                .entity("New product created").build();
    }

    @PUT
    @Loggable
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateProduct(Product product) {
        // Update an existing product
        Response response;
        System.out.println("Updating product");
        Product updatedProduct = productRepository.updateProduct(product);
        if (updatedProduct != null) {
            response = Response.status(Response.Status.OK)
                    .entity("Product updated").build();
        } else {
            response = Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found").build();
        }
        return response;
    }

    @DELETE
    @Loggable
    @Path("products/{id}")
    public Response deleteProduct(@PathParam("id") Long id) {
        // Delete a product
        Response response;
        System.out.println("Deleting product with id: " + id);
        Product product = productRepository.findProductById(id);
        if (product != null) {
            productRepository.deleteProduct(product);
            response = Response.status(Response.Status.OK)
                    .entity("Product deleted").build();
        } else {
            response = Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found").build();
        }
        return response;
    }
}