package io.microprofile.tutorial.store.inventory.integration;

import io.microprofile.tutorial.store.inventory.service.InventoryService;
import io.microprofile.tutorial.store.inventory.entity.Inventory;
import io.microprofile.tutorial.store.inventory.dto.Product;
import io.microprofile.tutorial.store.inventory.client.ProductServiceClient;
import io.microprofile.tutorial.store.inventory.repository.InventoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for InventoryService with ProductServiceClient.
 * This test class focuses on the main integration points.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceIntegrationTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private InventoryService inventoryService;

    private Product mockProduct;
    private Inventory mockInventory;

    @BeforeEach
    void setUp() throws Exception {
        // Create mock product using constructor
        mockProduct = new Product(1L, "Test Product", 29.99, "Electronics", "A test product");
        
        // Create mock inventory with proper productId set using reflection
        mockInventory = new Inventory();
        setPrivateField(mockInventory, "inventoryId", 1L);
        setPrivateField(mockInventory, "productId", 1L);
        setPrivateField(mockInventory, "quantity", 10);
        setPrivateField(mockInventory, "reservedQuantity", 2);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    @Test
    void testProductServiceClientIntegration_BasicCall() throws Exception {
        // Arrange
        lenient().when(productServiceClient.getProductById(anyLong())).thenReturn(mockProduct);

        // Act
        Product result = inventoryService.getProductInfo(mockInventory);

        // Assert
        assertNotNull(result);
        assertEquals(1L, getPrivateField(result, "id"));
        verify(productServiceClient).getProductById(1L);
    }

    @Test
    void testCreateInventory_CallsProductValidation() throws Exception {
        // Arrange
        Inventory newInventory = new Inventory();
        setPrivateField(newInventory, "productId", 1L);
        setPrivateField(newInventory, "quantity", 5);
        setPrivateField(newInventory, "reservedQuantity", 0);
        
        lenient().when(productServiceClient.getProductById(anyLong())).thenReturn(mockProduct);
        lenient().when(inventoryRepository.findByProductId(anyLong())).thenReturn(Optional.empty());
        lenient().when(inventoryRepository.save(any(Inventory.class))).thenReturn(mockInventory);

        // Act
        Inventory result = inventoryService.createInventory(newInventory);
            
        // Assert
        assertNotNull(result);
        verify(productServiceClient).getProductById(1L);
        verify(inventoryRepository).save(newInventory);
    }

    @Test
    void testUpdateInventory_CallsProductValidation() throws Exception {
        // Arrange
        Inventory updatedInventory = new Inventory();
        setPrivateField(updatedInventory, "productId", 1L);
        setPrivateField(updatedInventory, "quantity", 15);
        
        lenient().when(productServiceClient.getProductById(anyLong())).thenReturn(mockProduct);
        lenient().when(inventoryRepository.findByProductId(anyLong())).thenReturn(Optional.of(mockInventory));
        lenient().when(inventoryRepository.update(anyLong(), any(Inventory.class))).thenReturn(Optional.of(mockInventory));

        // Act
        Inventory result = inventoryService.updateInventory(1L, updatedInventory);
            
        // Assert
        assertNotNull(result);
        verify(productServiceClient).getProductById(1L);
        verify(inventoryRepository).update(1L, updatedInventory);
    }

    @Test
    void testProductServiceClient_ReturnsProductData() throws Exception {
        // Arrange
        lenient().when(productServiceClient.getProductById(anyLong())).thenReturn(mockProduct);

        // Act
        Product result = inventoryService.getProductInfo(mockInventory);

        // Assert
        assertNotNull(result);
        // Test using reflection to access private fields
        assertEquals("Test Product", getPrivateField(result, "name"));
        assertEquals("Electronics", getPrivateField(result, "category"));
        verify(productServiceClient).getProductById(1L);
    }

    @Test
    void testCreateInventory_WithInvalidProduct_ThrowsException() throws Exception {
        // Arrange
        Inventory newInventory = new Inventory();
        setPrivateField(newInventory, "productId", 999L);
        setPrivateField(newInventory, "quantity", 5);
        
        lenient().when(productServiceClient.getProductById(999L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.createInventory(newInventory);
        });
        
        assertTrue(exception.getMessage().contains("Product not found in catalog with ID: 999"));
        verify(productServiceClient).getProductById(999L);
    }
}