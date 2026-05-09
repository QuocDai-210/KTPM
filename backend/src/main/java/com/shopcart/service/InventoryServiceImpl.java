package com.shopcart.service;

import org.springframework.stereotype.Service;

import com.shopcart.repository.ProductRepository;

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
      productRepository.save(product.get());
    }
  }

  @Override
  public void increaseStock(String productId, int quantity) {
    var product = productRepository.findById(productId);
    if (product.isPresent()) {
      product.get().setStock(product.get().getStock() + quantity);
      productRepository.save(product.get());
    }
  }
}
