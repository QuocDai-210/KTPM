package com.shopcart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shopcart.entity.Inventory;
import com.shopcart.repository.InventoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Service Tests")
class InventoryServiceImplTest {
  @Mock private InventoryRepository inventoryRepository;

  @InjectMocks private InventoryServiceImpl inventoryService;

  @Test
  @DisplayName("isAvailable returns true when stock is sufficient")
  void testIsAvailableTrue() {
    when(inventoryRepository.findByProductId("P001")).thenReturn(Optional.of(new Inventory("P001", 5)));

    assertTrue(inventoryService.isAvailable("P001", 3));
  }

  @Test
  @DisplayName("isAvailable returns false when product is missing")
  void testIsAvailableFalseWhenProductMissing() {
    when(inventoryRepository.findByProductId("P404")).thenReturn(Optional.empty());

    assertFalse(inventoryService.isAvailable("P404", 1));
  }

  @Test
  @DisplayName("decreaseStock updates product and saves it")
  void testDecreaseStock() {
    Inventory inventory = new Inventory("P001", 5);
    when(inventoryRepository.findByProductId("P001")).thenReturn(Optional.of(inventory));

    inventoryService.decreaseStock("P001", 2);

    assertEquals(3, inventory.getQuantity());
    verify(inventoryRepository).save(inventory);
  }

  @Test
  @DisplayName("increaseStock updates product and saves it")
  void testIncreaseStock() {
    Inventory inventory = new Inventory("P001", 5);
    when(inventoryRepository.findByProductId("P001")).thenReturn(Optional.of(inventory));

    inventoryService.increaseStock("P001", 4);

    assertEquals(9, inventory.getQuantity());
    verify(inventoryRepository).save(inventory);
  }

  @Test
  @DisplayName("stock operations do nothing when product is missing")
  void testStockOperationsDoNothingWhenMissing() {
    when(inventoryRepository.findByProductId("P404")).thenReturn(Optional.empty());

    inventoryService.decreaseStock("P404", 2);
    inventoryService.increaseStock("P404", 2);

    verify(inventoryRepository, never()).save(any());
  }
}
