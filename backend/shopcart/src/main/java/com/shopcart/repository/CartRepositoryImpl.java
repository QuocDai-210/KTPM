package com.shopcart.repository;

import com.shopcart.entity.CartItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepositoryImpl implements CartRepository {
  private static final Map<String, CartItem> db = new HashMap<>();

  private String key(String userId, String productId) {
    return userId + ":" + productId;
  }

  @Override
  public CartItem save(CartItem item) {
    db.put(key("user01", item.getProductId()), item);
    return item;
  }

  @Override
  public Optional<CartItem> findByUserIdAndProductId(String userId, String productId) {
    return Optional.ofNullable(db.get(key(userId, productId)));
  }

  @Override
  public void deleteByUserIdAndProductId(String userId, String productId) {
    db.remove(key(userId, productId));
  }

  @Override
  public List<CartItem> findByUserId(String userId) {
    return db.values().stream().filter(it -> key(userId, it.getProductId()).startsWith(userId + ":")).collect(Collectors.toList());
  }
}
