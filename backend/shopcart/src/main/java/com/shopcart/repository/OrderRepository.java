package com.shopcart.repository;

import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import java.util.Optional;

public interface OrderRepository {
  Order save(Order order);
  Optional<Order> findById(String id);
  default Optional<Coupon> findCoupon(String code) { return Optional.empty(); }
}
