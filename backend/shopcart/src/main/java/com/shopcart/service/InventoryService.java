package com.shopcart.service;

public interface InventoryService {
  boolean isAvailable(String productId, int quantity);
  void decreaseStock(String productId, int quantity);
  void increaseStock(String productId, int quantity);
}
