package io.microprofile.tutorial.store.product.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

public class ProductResourceTest {
   private ProductResource productResource;

    @BeforeEach
    void setUp() {
        productResource = new ProductResource();
    }

    @Test
    void testGetProducts() {
        Response response = productResource.getProducts();
        List<Product> products = response.readEntity(new GenericType<List<Product>>() {});

        assertNotNull(products);
        assertEquals(2, products.size());
    } 
}