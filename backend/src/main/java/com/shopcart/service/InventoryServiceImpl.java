package com.shopcart.service;

import com.shopcart.repository.InventoryRepository;
import com.shopcart.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {
  private final InventoryRepository inventoryRepository;
  private final ProductRepository productRepository;

  public InventoryServiceImpl(
      InventoryRepository inventoryRepository, ProductRepository productRepository) {
    this.inventoryRepository = inventoryRepository;
    this.productRepository = productRepository;
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
      int nextQuantity = inventory.get().getQuantity() - quantity;
      inventory.get().setQuantity(nextQuantity);
      inventoryRepository.save(inventory.get());
      syncProductStock(productId, nextQuantity);
    }
  }

  @Override
  public void increaseStock(String productId, int quantity) {
    var inventory = inventoryRepository.findByProductId(productId);
    if (inventory.isPresent()) {
      int nextQuantity = inventory.get().getQuantity() + quantity;
      inventory.get().setQuantity(nextQuantity);
      inventoryRepository.save(inventory.get());
      syncProductStock(productId, nextQuantity);
    }
  }

  private void syncProductStock(String productId, int quantity) {
    productRepository
        .findById(productId)
        .ifPresent(
            product -> {
              product.setStock(quantity);
              productRepository.save(product);
            });
  }
}
