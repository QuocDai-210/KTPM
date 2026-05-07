package com.shopcart.repository;

import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
  private static final Map<String, Order> orders = new HashMap<>();
  private static final Map<String, Coupon> coupons = new HashMap<>();

  static {
    coupons.put("SALE10", new Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31"));
    coupons.put("SALE20", new Coupon("SALE20", "PERCENT", 20L, 0L, "2026-12-31"));
    coupons.put("FIXED100K", new Coupon("FIXED100K", "FIXED", 100000L, 0L, "2026-12-31"));
    coupons.put("EXPIRED2023", new Coupon("EXPIRED2023", "PERCENT", 10L, 0L, "2023-12-31"));
  }

  @Override
  public Order save(Order order) {
    orders.put(order.getId(), order);
    return order;
  }

  @Override
  public Optional<Order> findById(String id) {
    return Optional.ofNullable(orders.get(id));
  }

  @Override
  public Optional<Coupon> findCoupon(String code) {
    return Optional.ofNullable(coupons.get(code));
  }
}
