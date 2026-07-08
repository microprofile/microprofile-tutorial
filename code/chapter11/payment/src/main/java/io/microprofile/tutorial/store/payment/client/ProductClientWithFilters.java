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
import io.microprofile.tutorial.store.payment.filter.BearerTokenFilter;
import io.microprofile.tutorial.store.payment.filter.CorrelationIdFilter;
import io.microprofile.tutorial.store.payment.filter.RequestLoggingFilter;
import io.microprofile.tutorial.store.payment.filter.ResponseLoggingFilter;

import java.util.List;

/**
 * MicroProfile Rest Client with custom filters and interceptors registered.
 * 
 * This interface demonstrates:
 * - Registering multiple filters using @RegisterProvider
 * - Specifying filter priority to control execution order
 * - Combining authentication, logging, and tracing filters
 * - How filters execute in the request/response lifecycle
 * 
 * Filter Execution Order (Request):
 * 1. BearerTokenFilter (Priority 1000 - AUTHENTICATION)
 *    - Adds Authorization header with Bearer token
 * 2. CorrelationIdFilter (Priority 100)
 *    - Adds X-Correlation-ID and X-Request-ID headers
 * 3. RequestLoggingFilter (Priority 300)
 *    - Logs complete request details
 * 
 * Filter Execution Order (Response):
 * 1. ResponseLoggingFilter (Priority 300)
 *    - Logs complete response details
 * 
 * Compare this client with ProductClient to see the difference in logging output.
 * 
 * Configuration properties (in microprofile-config.properties):
 * - catalog-service-filtered/mp-rest/url=http://localhost:5050/catalog/api
 * - catalog-service-filtered/mp-rest/scope=jakarta.enterprise.context.ApplicationScoped
 * - catalog-service.bearer.token (optional)
 */
@RegisterRestClient(configKey = "catalog-service-filtered")
@RegisterProvider(value = BearerTokenFilter.class, priority = 1000)
@RegisterProvider(value = CorrelationIdFilter.class, priority = 100)
@RegisterProvider(value = RequestLoggingFilter.class, priority = 300)
@RegisterProvider(value = ResponseLoggingFilter.class, priority = 300)
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public interface ProductClientWithFilters extends AutoCloseable {

    /**
     * Retrieves all products from the catalog service.
     * 
     * When called, you will see filter execution in the logs:
     * 1. BearerTokenFilter adds authentication
     * 2. CorrelationIdFilter adds tracking IDs
     * 3. RequestLoggingFilter logs the outgoing request
     * 4. HTTP request is sent
     * 5. HTTP response is received
     * 6. ResponseLoggingFilter logs the incoming response
     * 7. Response is deserialized to List<Product>
     * 
     * @return List of all products
     * @throws RuntimeException if service returns 5xx error
     */
    @GET
    List<Product> getAllProducts();

    /**
     * Retrieves a specific product by its ID.
     * 
     * Demonstrates the complete filter chain with path parameters.
     * Check the logs to see how filters handle parameterized requests.
     * 
     * @param id The product ID
     * @return The product with the specified ID
     * @throws ProductNotFoundException if product is not found (404)
     */
    @GET
    @Path("/{id}")
    Product getProductById(@PathParam("id") Long id) throws ProductNotFoundException;
}
