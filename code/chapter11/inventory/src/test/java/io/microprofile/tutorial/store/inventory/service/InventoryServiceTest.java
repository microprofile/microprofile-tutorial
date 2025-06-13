package io.microprofile.tutorial.store.inventory.service;

import io.microprofile.tutorial.store.inventory.entity.Inventory;
import io.microprofile.tutorial.store.inventory.exception.InventoryNotFoundException;
import io.microprofile.tutorial.store.inventory.exception.InventoryConflictException;
import io.microprofile.tutorial.store.inventory.repository.InventoryRepository;
import io.microprofile.tutorial.store.inventory.client.ProductServiceClient;
import io.microprofile.tutorial.store.inventory.dto.Product;
import io.microprofile.tutorial.store.inventory.dto.InventoryWithProductInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryService with ProductServiceClient integration.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private InventoryService inventoryService;

    private Product mockProduct;
    private Inventory mockInventory;

    @BeforeEach
    void setUp() {
        mockProduct = new Product(1L, "Test Product", 29.99, "Electronics", "A test product");
        
        mockInventory = new Inventory();
        mockInventory.setInventoryId(1L);
        mockInventory.setProductId(1L);
        mockInventory.setQuantity(100);
        mockInventory.setReservedQuantity(10);
    }

    @Test
    void testCreateInventory_WithValidProduct_ShouldSucceed() {
        // Arrange
        Inventory newInventory = Inventory.builder()
                .productId(1L)
                .quantity(50)
                .reservedQuantity(0)
                .build();

        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(mockInventory);

        // Act
        Inventory result = inventoryService.createInventory(newInventory);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getInventoryId());
        verify(productServiceClient).getProductById(1L);
        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository).save(newInventory);
    }

    @Test
    void testCreateInventory_WithInvalidProduct_ShouldThrowNotFoundException() {
        // Arrange
        Inventory newInventory = Inventory.builder()
                .productId(999L)
                .quantity(50)
                .reservedQuantity(0)
                .build();

        when(productServiceClient.getProductById(999L)).thenReturn(null);

        // Act & Assert
        InventoryNotFoundException exception = assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.createInventory(newInventory)
        );
        
        assertTrue(exception.getMessage().contains("Product not found in catalog with ID: 999"));
        verify(productServiceClient).getProductById(999L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testCreateInventory_WithExistingInventory_ShouldThrowConflictException() {
        // Arrange
        Inventory newInventory = Inventory.builder()
                .productId(1L)
                .quantity(50)
                .reservedQuantity(0)
                .build();

        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        // Act & Assert
        InventoryConflictException exception = assertThrows(
                InventoryConflictException.class,
                () -> inventoryService.createInventory(newInventory)
        );
        
        assertTrue(exception.getMessage().contains("Inventory for product already exists"));
        verify(productServiceClient).getProductById(1L);
        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testUpdateInventory_WithValidProduct_ShouldSucceed() {
        // Arrange
        Inventory updatedInventory = Inventory.builder()
                .productId(1L)
                .quantity(75)
                .reservedQuantity(5)
                .build();

        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.update(1L, updatedInventory)).thenReturn(Optional.of(mockInventory));

        // Act
        Inventory result = inventoryService.updateInventory(1L, updatedInventory);

        // Assert
        assertNotNull(result);
        verify(productServiceClient).getProductById(1L);
        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository).update(1L, updatedInventory);
    }

    @Test
    void testUpdateInventory_WithProductConflict_ShouldThrowConflictException() {
        // Arrange
        Inventory existingInventory = Inventory.builder()
                .inventoryId(2L)
                .productId(1L)
                .quantity(25)
                .reservedQuantity(0)
                .build();

        Inventory updatedInventory = Inventory.builder()
                .productId(1L)
                .quantity(75)
                .reservedQuantity(5)
                .build();

        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(existingInventory));

        // Act & Assert
        InventoryConflictException exception = assertThrows(
                InventoryConflictException.class,
                () -> inventoryService.updateInventory(1L, updatedInventory)
        );
        
        assertTrue(exception.getMessage().contains("Another inventory record already exists"));
        verify(productServiceClient).getProductById(1L);
        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository, never()).update(anyLong(), any());
    }

    @Test
    void testCreateBulkInventories_WithValidProducts_ShouldSucceed() {
        // Arrange
        Product product2 = new Product(2L, "Product 2", 19.99, "Home", "Another product");
        
        Inventory inventory1 = Inventory.builder().productId(1L).quantity(50).reservedQuantity(0).build();
        Inventory inventory2 = Inventory.builder().productId(2L).quantity(25).reservedQuantity(0).build();
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);

        Inventory saved1 = Inventory.builder().inventoryId(1L).productId(1L).quantity(50).reservedQuantity(0).build();
        Inventory saved2 = Inventory.builder().inventoryId(2L).productId(2L).quantity(25).reservedQuantity(0).build();

        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);
        when(productServiceClient.getProductById(2L)).thenReturn(product2);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(inventory1)).thenReturn(saved1);
        when(inventoryRepository.save(inventory2)).thenReturn(saved2);

        // Act
        List<Inventory> result = inventoryService.createBulkInventories(inventories);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productServiceClient).getProductById(1L);
        verify(productServiceClient).getProductById(2L);
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
    }

    @Test
    void testGetInventoryWithProductInfo_ShouldReturnEnrichedData() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(mockInventory));
        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);

        // Act
        InventoryWithProductInfo result = inventoryService.getInventoryWithProductInfo(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockInventory, result.getInventory());
        assertEquals(mockProduct, result.getProduct());
        assertEquals("Test Product", result.getProductName());
        assertEquals(29.99, result.getProductPrice());
        assertEquals("Electronics", result.getProductCategory());
        assertEquals(100, result.getQuantity());
        assertEquals(10, result.getReservedQuantity());
        assertEquals(90, result.getAvailableQuantity());
    }

    @Test
    void testGetInventoriesByCategory_ShouldReturnFilteredInventories() {
        // Arrange
        Product product2 = new Product(2L, "Product 2", 39.99, "Electronics", "Another electronics product");
        List<Product> electronicsProducts = Arrays.asList(mockProduct, product2);

        Inventory inventory2 = Inventory.builder()
                .inventoryId(2L)
                .productId(2L)
                .quantity(75)
                .reservedQuantity(5)
                .build();

        when(productServiceClient.getProductsByCategory("Electronics")).thenReturn(electronicsProducts);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.of(inventory2));

        // Act
        List<InventoryWithProductInfo> result = inventoryService.getInventoriesByCategory("Electronics");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        InventoryWithProductInfo first = result.get(0);
        assertEquals("Test Product", first.getProductName());
        assertEquals("Electronics", first.getProductCategory());
        
        InventoryWithProductInfo second = result.get(1);
        assertEquals("Product 2", second.getProductName());
        assertEquals("Electronics", second.getProductCategory());
        
        verify(productServiceClient).getProductsByCategory("Electronics");
        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository).findByProductId(2L);
    }

    @Test
    void testGetProductInfo_ShouldReturnProductDetails() {
        // Arrange
        when(productServiceClient.getProductById(1L)).thenReturn(mockProduct);

        // Act
        Product result = inventoryService.getProductInfo(mockInventory);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(29.99, result.getPrice());
        assertEquals("Electronics", result.getCategory());
        verify(productServiceClient).getProductById(1L);
    }

    @Test
    void testValidateProductExists_WithServiceError_ShouldThrowRuntimeException() {
        // Arrange
        WebApplicationException serviceException = new WebApplicationException(
                Response.status(500).build()
        );
        when(productServiceClient.getProductById(1L)).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inventoryService.createInventory(mockInventory)
        );
        
        assertTrue(exception.getMessage().contains("Failed to validate product with catalog service"));
        verify(productServiceClient).getProductById(1L);
    }
}
