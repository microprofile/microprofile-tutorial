package io.microprofile.tutorial.store.product.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.service.ProductService;

@ExtendWith(MockitoExtension.class)
public class ProductResourceTest {
    @Mock
    private ProductService productService;
    
    @InjectMocks
    private ProductResource productResource;

    @BeforeEach
    void setUp() {
        // Setup is handled by MockitoExtension
    }

    @AfterEach
    void tearDown() {
        // Cleanup is handled by MockitoExtension
    }

    @Test
    void testGetProducts() {
        // Prepare test data
        List<Product> mockProducts = new ArrayList<>();
        
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone");
        product1.setDescription("Apple iPhone 15");
        product1.setPrice(999.99);
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("MacBook");
        product2.setDescription("Apple MacBook Air");
        product2.setPrice(1299.0);
        
        mockProducts.add(product1);
        mockProducts.add(product2);
        
        // Mock the service method
        when(productService.findAllProducts()).thenReturn(mockProducts);
        
        // Call the method under test
        Response response = productResource.getAllProducts();
        
        // Assertions
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) response.getEntity();
        assertNotNull(products);
        assertEquals(2, products.size());
    } 
}