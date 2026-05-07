package com.shopcart.repository;

import com.shopcart.entity.Product;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
  private static final Map<String, Product> db = new HashMap<>();

  static {
    db.put("P001", new Product("P001", "Laptop Dell", 15000000L, 10));
    db.put("P002", new Product("P002", "Mouse Logitech", 500000L, 50));
    db.put("P003", new Product("P003", "Keyboard Mechanical", 2000000L, 0));
  }

  @Override
  public Optional<Product> findById(String id) {
    return Optional.ofNullable(db.get(id));
  }
}
