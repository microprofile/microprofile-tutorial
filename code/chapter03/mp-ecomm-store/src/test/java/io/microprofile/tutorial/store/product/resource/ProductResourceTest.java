package io.microprofile.tutorial.store.product.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.service.ProductService;

public class ProductResourceTest {
    private ProductResource productResource;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
        productResource = new ProductResource(productService);
    }

    @AfterEach
    void tearDown() {
        productResource = null;
        productService = null;
    }

    @Test
    void testGetAllProducts() {
        // Call the method to test
        Response response = productResource.getAllProducts();
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // Assert the entity content
        List<Product> products = (List<Product>) response.getEntity();
        assertNotNull(products);
        assertEquals(2, products.size());
    }
    
    @Test
    void testGetProductById_ExistingProduct() {
        // Call the method to test
        Response response = productResource.getProductById(1L);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // Assert the entity content
        Product product = (Product) response.getEntity();
        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("iPhone", product.getName());
    }
    
    @Test
    void testGetProductById_NonExistingProduct() {
        // Call the method to test
        Response response = productResource.getProductById(999L);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    void testCreateProduct() {
        // Create a product to add
        Product newProduct = new Product(3L, "iPad", "Apple iPad Pro", 799.99);
        
        // Call the method to test
        Response response = productResource.createProduct(newProduct);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        
        // Assert the entity content
        Product createdProduct = (Product) response.getEntity();
        assertNotNull(createdProduct);
        assertEquals(3L, createdProduct.getId());
        assertEquals("iPad", createdProduct.getName());
        
        // Verify the product was added to the list
        Response getAllResponse = productResource.getAllProducts();
        List<Product> allProducts = (List<Product>) getAllResponse.getEntity();
        assertEquals(3, allProducts.size());
    }
    
    @Test
    void testUpdateProduct_ExistingProduct() {
        // Create an updated product
        Product updatedProduct = new Product(1L, "iPhone Pro", "Apple iPhone 15 Pro", 1199.99);
        
        // Call the method to test
        Response response = productResource.updateProduct(1L, updatedProduct);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // Assert the entity content
        Product returnedProduct = (Product) response.getEntity();
        assertNotNull(returnedProduct);
        assertEquals(1L, returnedProduct.getId());
        assertEquals("iPhone Pro", returnedProduct.getName());
        assertEquals("Apple iPhone 15 Pro", returnedProduct.getDescription());
        assertEquals(1199.99, returnedProduct.getPrice());
        
        // Verify the product was updated in the list
        Response getResponse = productResource.getProductById(1L);
        Product retrievedProduct = (Product) getResponse.getEntity();
        assertEquals("iPhone Pro", retrievedProduct.getName());
        assertEquals(1199.99, retrievedProduct.getPrice());
    }
    
    @Test
    void testUpdateProduct_NonExistingProduct() {
        // Create a product with non-existent ID
        Product nonExistentProduct = new Product(999L, "Nonexistent", "This product doesn't exist", 0.0);
        
        // Call the method to test
        Response response = productResource.updateProduct(999L, nonExistentProduct);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    void testDeleteProduct_ExistingProduct() {
        // Call the method to test
        Response response = productResource.deleteProduct(1L);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        
        // Verify the product was deleted
        Response getResponse = productResource.getProductById(1L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
        
        // Verify the total count is reduced
        Response getAllResponse = productResource.getAllProducts();
        List<Product> allProducts = (List<Product>) getAllResponse.getEntity();
        assertEquals(1, allProducts.size());
    }
    
    @Test
    void testDeleteProduct_NonExistingProduct() {
        // Call the method to test with non-existent ID
        Response response = productResource.deleteProduct(999L);
        
        // Assert response properties
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        
        // Verify list size remains unchanged
        Response getAllResponse = productResource.getAllProducts();
        List<Product> allProducts = (List<Product>) getAllResponse.getEntity();
        assertEquals(2, allProducts.size());
    }
}