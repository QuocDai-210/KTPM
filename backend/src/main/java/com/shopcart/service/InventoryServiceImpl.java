package com.shopcart.service;

import com.shopcart.repository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {
  private final InventoryRepository inventoryRepository;

  public InventoryServiceImpl(InventoryRepository inventoryRepository) {
    this.inventoryRepository = inventoryRepository;
  }

  @Override
  public boolean isAvailable(String productId, int quantity) {
    var inventory = inventoryRepository.findByProductId(productId);
    if (inventory.isEmpty()) return false;
    return inventory.get().getQuantity() >= quantity;
  }

  @Override
  public void decreaseStock(String productId, int quantity) {
    var inventory = inventoryRepository.findByProductId(productId);
    if (inventory.isPresent()) {
      inventory.get().setQuantity(inventory.get().getQuantity() - quantity);
      inventoryRepository.save(inventory.get());
    }
  }

  @Override
  public void increaseStock(String productId, int quantity) {
    var inventory = inventoryRepository.findByProductId(productId);
    if (inventory.isPresent()) {
      inventory.get().setQuantity(inventory.get().getQuantity() + quantity);
      inventoryRepository.save(inventory.get());
    }
  }
}
