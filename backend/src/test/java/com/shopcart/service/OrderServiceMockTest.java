package com.shopcart.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopcart.entity.Inventory;
import com.shopcart.entity.Product;
import com.shopcart.repository.InventoryRepository;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceMockTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductRepository productRepository;

  @Mock private InventoryRepository inventoryRepository;

  @InjectMocks private InventoryServiceImpl inventoryService;

  @Test
  void testCheckAndReduceInventory() {
    Inventory inventory = new Inventory("P001", 10);
    Product product = new Product("P001", "Laptop", 1000L, 10);

    when(inventoryRepository.findByProductId("P001")).thenReturn(Optional.of(inventory));
    when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
    when(productRepository.findById("P001")).thenReturn(Optional.of(product));

    inventoryService.decreaseStock("P001", 3);

    ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
    verify(inventoryRepository).save(captor.capture());
    verify(productRepository).save(product);
    verifyNoInteractions(orderRepository);
    assertEquals(7, captor.getValue().getQuantity());
    assertEquals(7, product.getStock());
  }

  @Test
  void testCheckInventoryAvailable() {
    when(inventoryRepository.findByProductId("P001")).thenReturn(Optional.of(new Inventory("P001", 10)));

    boolean available = inventoryService.isAvailable("P001", 3);

    assertTrue(available);
    verify(inventoryRepository).findByProductId("P001");
    verifyNoInteractions(orderRepository, productRepository);
  }

  @Test
  void testCheckInventoryNotEnough() {
    when(inventoryRepository.findByProductId("P001")).thenReturn(Optional.of(new Inventory("P001", 2)));

    boolean available = inventoryService.isAvailable("P001", 3);

    assertFalse(available);
    verify(inventoryRepository).findByProductId("P001");
    verifyNoInteractions(orderRepository, productRepository);
  }

  @Test
  void testDoNothingWhenInventoryNotFound() {
    when(inventoryRepository.findByProductId("P404")).thenReturn(Optional.empty());

    inventoryService.decreaseStock("P404", 3);

    verify(inventoryRepository, never()).save(any());
    verifyNoInteractions(orderRepository, productRepository);
  }
}
