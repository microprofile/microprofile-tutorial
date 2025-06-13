package io.microprofile.tutorial.store.inventory.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.microprofile.tutorial.store.inventory.dto.Product;

@RegisterRestClient(configKey = "product-service")
@Path("/products")
public interface ProductServiceClient extends AutoCloseable {

    @GET
    @Path("/{id}")
    Product getProductById(@PathParam("id") Long id);

    @GET
    List<Product> getProductsByCategory(@QueryParam("category") String category);
}