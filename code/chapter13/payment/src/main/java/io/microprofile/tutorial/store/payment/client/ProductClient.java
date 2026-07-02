package io.microprofile.tutorial.store.payment.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

import io.microprofile.tutorial.store.payment.dto.product.Product;
import io.microprofile.tutorial.store.payment.exception.ProductNotFoundException;

import java.util.List;

/**
 * MicroProfile Rest Client interface for the Catalog/Product Service.
 * 
 * This interface demonstrates:
 * - @RegisterRestClient annotation to register as a REST client
 * - @RegisterProvider to register custom exception mapper
 * - configKey for external configuration via MicroProfile Config
 * - Type-safe method definitions with Jakarta REST annotations
 * - Automatic implementation generation by MicroProfile runtime
 * - Custom error handling via ResponseExceptionMapper
 * 
 * Configuration properties (in microprofile-config.properties):
 * - catalog-service/mp-rest/url=http://localhost:5050/catalog/api
 * - catalog-service/mp-rest/scope=jakarta.enterprise.context.ApplicationScoped
 * 
 * This interface extends AutoCloseable to support try-with-resources pattern
 * when using RestClientBuilder for programmatic client creation.
 */
@RegisterRestClient(configKey = "catalog-service")
@RegisterProvider(ProductServiceResponseExceptionMapper.class)
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public interface ProductClient extends AutoCloseable {

    /**
     * Retrieves all products from the catalog service.
     * 
     * @return List of all products
     * @throws RuntimeException if service returns 5xx error
     */
    @GET
    List<Product> getAllProducts();

    /**
     * Retrieves a specific product by its ID.
     * 
     * Example usage: productClient.getProductById(1L)
     * Resulting HTTP request: GET /products/1
     * 
     * Demonstrates checked exception handling:
     * - Throws ProductNotFoundException if product not found (404)
     * - Method must declare this checked exception in throws clause
     * 
     * @param id The product ID
     * @return The product with the specified ID
     * @throws ProductNotFoundException if product is not found (404)
     */
    @GET
    @Path("/{id}")
    Product getProductById(@PathParam("id") Long id) throws ProductNotFoundException;
}
