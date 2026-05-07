package com.shopcart.service;

import com.shopcart.repository.ProductRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {
  private final ProductRepository productRepository;

  public InventoryServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public boolean isAvailable(String productId, int quantity) {
    var product = productRepository.findById(productId);
    if (product.isEmpty()) return false;
    return product.get().getStock() >= quantity;
  }

  @Override
  public void decreaseStock(String productId, int quantity) {
    var product = productRepository.findById(productId);
    if (product.isPresent()) {
      product.get().setStock(product.get().getStock() - quantity);
    }
  }

  @Override
  public void increaseStock(String productId, int quantity) {
    var product = productRepository.findById(productId);
    if (product.isPresent()) {
      product.get().setStock(product.get().getStock() + quantity);
    }
  }
}
