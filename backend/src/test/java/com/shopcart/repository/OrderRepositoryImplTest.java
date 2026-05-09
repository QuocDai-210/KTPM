package com.shopcart.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcart.entity.Order;
import com.shopcart.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order Repository Tests")
class OrderRepositoryImplTest {
  private OrderRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    repository = new OrderRepositoryImpl();
    repository.clear();
  }

  @Test
  @DisplayName("save generates order id when missing")
  void testSaveGeneratesId() {
    Order order = new Order();
    order.setUserId("user01");
    order.setStatus(OrderStatus.PENDING);

    Order saved = repository.save(order);

    assertNotNull(saved.getId());
    assertTrue(saved.getId().startsWith("ORD-"));
  }

  @Test
  @DisplayName("save preserves existing order id")
  void testSavePreservesExistingId() {
    Order order = new Order();
    order.setId("ORD-CUSTOM-001");
    order.setUserId("user01");

    Order saved = repository.save(order);

    assertEquals("ORD-CUSTOM-001", saved.getId());
  }

  @Test
  @DisplayName("findById returns saved order")
  void testFindById() {
    Order order = new Order();
    order.setId("ORD-1001");
    order.setUserId("user01");
    repository.save(order);

    var result = repository.findById("ORD-1001");

    assertTrue(result.isPresent());
    assertEquals("user01", result.get().getUserId());
  }

  @Test
  @DisplayName("findCoupon returns configured coupon and empty for unknown code")
  void testFindCoupon() {
    assertTrue(repository.findCoupon("SALE10").isPresent());
    assertFalse(repository.findCoupon("UNKNOWN").isPresent());
  }
}
