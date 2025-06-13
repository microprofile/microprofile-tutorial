package io.microprofile.tutorial.store.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.microprofile.tutorial.store.product.entity.Product;

public class ProductServiceTest {
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @AfterEach
    void tearDown() {
        productService = null;
    }

    @Test
    void testGetAllProducts() {
        // Call the method to test
        List<Product> products = productService.getAllProducts();
        
        // Assert the result
        assertNotNull(products);
        assertEquals(2, products.size());
    }
    
    @Test
    void testGetProductById_ExistingProduct() {
        // Call the method to test
        Optional<Product> product = productService.getProductById(1L);
        
        // Assert the result
        assertTrue(product.isPresent());
        assertEquals("iPhone", product.get().getName());
        assertEquals("Apple iPhone 15", product.get().getDescription());
        assertEquals(999.99, product.get().getPrice());
    }
    
    @Test
    void testGetProductById_NonExistingProduct() {
        // Call the method to test
        Optional<Product> product = productService.getProductById(999L);
        
        // Assert the result
        assertFalse(product.isPresent());
    }
    
    @Test
    void testCreateProduct() {
        // Create a product to add
        Product newProduct = new Product(3L, "iPad", "Apple iPad Pro", 799.99);
        
        // Call the method to test
        Product createdProduct = productService.createProduct(newProduct);
        
        // Assert the result
        assertNotNull(createdProduct);
        assertEquals(3L, createdProduct.getId());
        assertEquals("iPad", createdProduct.getName());
        
        // Verify the product was added to the list
        List<Product> allProducts = productService.getAllProducts();
        assertEquals(3, allProducts.size());
    }
    
    @Test
    void testUpdateProduct_ExistingProduct() {
        // Create an updated product
        Product updatedProduct = new Product(1L, "iPhone Pro", "Apple iPhone 15 Pro", 1199.99);
        
        // Call the method to test
        Optional<Product> result = productService.updateProduct(1L, updatedProduct);
        
        // Assert the result
        assertTrue(result.isPresent());
        assertEquals("iPhone Pro", result.get().getName());
        assertEquals("Apple iPhone 15 Pro", result.get().getDescription());
        assertEquals(1199.99, result.get().getPrice());
        
        // Verify the product was updated in the list
        Optional<Product> updatedInList = productService.getProductById(1L);
        assertTrue(updatedInList.isPresent());
        assertEquals("iPhone Pro", updatedInList.get().getName());
    }
    
    @Test
    void testUpdateProduct_NonExistingProduct() {
        // Create an updated product
        Product updatedProduct = new Product(999L, "Nonexistent Product", "This product doesn't exist", 0.0);
        
        // Call the method to test
        Optional<Product> result = productService.updateProduct(999L, updatedProduct);
        
        // Assert the result
        assertFalse(result.isPresent());
    }
    
    @Test
    void testDeleteProduct_ExistingProduct() {
        // Call the method to test
        boolean deleted = productService.deleteProduct(1L);
        
        // Assert the result
        assertTrue(deleted);
        
        // Verify the product was removed from the list
        List<Product> allProducts = productService.getAllProducts();
        assertEquals(1, allProducts.size());
        assertFalse(productService.getProductById(1L).isPresent());
    }
    
    @Test
    void testDeleteProduct_NonExistingProduct() {
        // Call the method to test
        boolean deleted = productService.deleteProduct(999L);
        
        // Assert the result
        assertFalse(deleted);
        
        // Verify the list is unchanged
        List<Product> allProducts = productService.getAllProducts();
        assertEquals(2, allProducts.size());
    }
}
