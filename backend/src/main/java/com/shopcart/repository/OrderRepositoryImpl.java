package com.shopcart.repository;

import com.shopcart.database.seed.CouponSeedData;
import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
  private static final Map<String, Order> orders = new HashMap<>();
  private static final Map<String, Coupon> coupons = new HashMap<>();
  private static final AtomicInteger sequence = new AtomicInteger(1000);

  public OrderRepositoryImpl() {
    coupons.clear();
    coupons.putAll(CouponSeedData.loadCoupons());
  }

  @Override
  public Order save(Order order) {
    if (order.getId() == null || order.getId().isBlank()) {
      order.setId("ORD-" + sequence.incrementAndGet());
    }
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

  void clear() {
    orders.clear();
    sequence.set(1000);
  }
}
