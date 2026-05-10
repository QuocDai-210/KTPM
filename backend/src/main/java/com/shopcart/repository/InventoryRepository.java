package com.shopcart.repository;

import com.shopcart.entity.Inventory;
import java.util.Optional;

public interface InventoryRepository {
  Optional<Inventory> findByProductId(String productId);

  Inventory save(Inventory inventory);
}
