package io.microprofile.tutorial.store.product.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import io.microprofile.tutorial.store.product.entity.Product;

public class ProductResourceTest {
   private ProductResource productResource;

    @BeforeEach
    void setUp() {
        productResource = new ProductResource();
    }

    @AfterEach
    void tearDown() {
        productResource = null;
    }

    @Test
    void testGetProducts() {
        Response response = productResource.getAllProducts();
        
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        List<Product> products = (List<Product>) response.getEntity();
        assertNotNull(products);
        assertEquals(2, products.size());
    } 
}