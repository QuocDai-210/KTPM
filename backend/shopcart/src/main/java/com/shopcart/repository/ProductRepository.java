package com.shopcart.repository;

import com.shopcart.entity.Product;
import java.util.Optional;

public interface ProductRepository {
  Optional<Product> findById(String id);
}
