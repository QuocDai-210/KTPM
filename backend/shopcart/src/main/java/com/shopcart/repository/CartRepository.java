package com.shopcart.repository;

import com.shopcart.entity.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartRepository {
  CartItem save(CartItem item);
  Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
  void deleteByUserIdAndProductId(String userId, String productId);
  List<CartItem> findByUserId(String userId);
}
