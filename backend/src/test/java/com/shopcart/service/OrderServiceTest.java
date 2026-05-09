package com.shopcart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.OrderStatus;
import com.shopcart.entity.Product;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Order Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductRepository productRepository;

  @Mock private InventoryService inventoryService;

  @InjectMocks private OrderService orderService;

  @Nested
  @DisplayName("createOrder()")
  class CreateOrderTest {

    @Test
    @DisplayName("TC1: Tao don hang va tru ton kho")
    void testCreateOrderSuccess() {
      OrderRequest request =
          OrderRequest.builder()
              .userId("user01")
              .items(
                  List.of(
                      new OrderItemRequest("P001", 2, 15000000L),
                      new OrderItemRequest("P002", 1, 500000L)))
              .couponCode("SALE10")
              .shippingFee(50000L)
              .shippingAddress("123 Test Street, HCM")
              .paymentMethod("COD")
              .build();

      when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
      when(inventoryService.isAvailable("P002", 1)).thenReturn(true);
      when(productRepository.findById("P001"))
          .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
      when(productRepository.findById("P002"))
          .thenReturn(Optional.of(new Product("P002", "Mouse Logitech", 500000L, 50)));
      when(orderRepository.findCoupon("SALE10"))
          .thenReturn(Optional.of(new Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31")));
      when(orderRepository.save(any(Order.class)))
          .thenAnswer(
              inv -> {
                Order o = inv.getArgument(0);
                o.setId(UUID.randomUUID().toString());
                return o;
              });

      OrderResponse response = orderService.createOrder(request);

      assertNotNull(response.getOrderId());
      assertEquals(OrderStatus.PENDING, response.getStatus());
      assertEquals(27500000L, response.getTotalPrice());
      verify(inventoryService, times(1)).decreaseStock("P001", 2);
      verify(inventoryService, times(1)).decreaseStock("P002", 1);
    }

    @Test
    @DisplayName("TC1.1: Tao don hang that bai khi thieu dia chi giao hang")
    void testCreateOrderMissingShippingAddress() {
      OrderRequest request =
          OrderRequest.builder()
              .userId("user01")
              .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
              .shippingFee(50000L)
              .paymentMethod("COD")
              .build();

      assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
      verify(orderRepository, never()).save(any(Order.class));
      verify(inventoryService, never()).decreaseStock(any(), anyInt());
    }
  }

  @Nested
  @DisplayName("getOrderById()")
  class GetOrderByIdTest {

    @Test
    @DisplayName("TC2: Lay thong tin don hang")
    void testGetOrderById() {
      Order order = new Order();
      order.setId("ORD-001");
      order.setUserId("user01");
      order.setStatus(OrderStatus.PENDING);
      order.setTotalPrice(27500000L);

      when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));

      Order result = orderService.getOrderById("ORD-001");

      assertNotNull(result);
      assertEquals("ORD-001", result.getId());
      assertEquals("user01", result.getUserId());
      assertEquals(OrderStatus.PENDING, result.getStatus());
      verify(orderRepository, times(1)).findById("ORD-001");
    }
  }

  @Nested
  @DisplayName("cancelOrder()")
  class CancelOrderTest {

    @Test
    @DisplayName("TC3: Huy don va hoan ton kho")
    void testCancelOrder() {
      Order order = new Order();
      order.setId("ORD-001");
      order.setStatus(OrderStatus.PENDING);
      order.setItems(
          List.of(
              new OrderItem("P001", 2, 15000000L),
              new OrderItem("P002", 1, 500000L)));

      when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));
      when(orderRepository.save(any(Order.class))).thenReturn(order);

      orderService.cancelOrder("ORD-001");

      assertEquals(OrderStatus.CANCELLED, order.getStatus());
      verify(inventoryService, times(1)).increaseStock("P001", 2);
      verify(inventoryService, times(1)).increaseStock("P002", 1);
      verify(orderRepository, times(1)).save(order);
    }
  }

  @Nested
  @DisplayName("calculateOrderTotal()")
  class CalculateOrderTotalTest {

    @Test
    @DisplayName("TC4: Tinh tong gia chinh xac")
    void testCalculateOrderTotal() {
      OrderRequest request =
          OrderRequest.builder()
              .items(
                  List.of(
                      new OrderItemRequest("P001", 2, 15000000L),
                      new OrderItemRequest("P002", 1, 500000L)))
              .couponCode("SALE10")
              .shippingFee(50000L)
              .build();

      when(productRepository.findById("P001"))
          .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
      when(productRepository.findById("P002"))
          .thenReturn(Optional.of(new Product("P002", "Mouse Logitech", 500000L, 50)));
      when(orderRepository.findCoupon("SALE10"))
          .thenReturn(Optional.of(new Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31")));

      Long totalPrice = orderService.calculateOrderTotal(request);

      assertEquals(27500000L, totalPrice);
    }
  }

  @Nested
  @DisplayName("checkStockBeforeOrder()")
  class CheckStockBeforeOrderTest {

    @Test
    @DisplayName("TC5: Kiem tra ton kho")
    void testCheckStockBeforeOrder() {
      OrderRequest request =
          OrderRequest.builder()
              .userId("user01")
              .items(List.of(new OrderItemRequest("P001", 11, 15000000L)))
              .shippingAddress("123 Test Street, HCM")
              .paymentMethod("COD")
              .build();

      when(inventoryService.isAvailable("P001", 11)).thenReturn(false);

      assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
      verify(inventoryService, times(1)).isAvailable("P001", 11);
      verify(orderRepository, never()).save(any(Order.class));
    }
  }
}
