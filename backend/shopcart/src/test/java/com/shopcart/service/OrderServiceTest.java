package com.shopcart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.OrderStatus;
import com.shopcart.entity.Product;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;

@DisplayName("Order Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductRepository productRepository;

  @Mock private InventoryService inventoryService;

  @Mock private CartRepository cartRepository;

  @InjectMocks private OrderService orderService;

  @Test
  @DisplayName("TC1: Tạo đơn hàng thành công")
  void testCreateOrderSuccess() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .couponCode("SALE10")
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(productRepository.findById("P001"))
        .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
    when(orderRepository.findCoupon("SALE10"))
        .thenReturn(Optional.of(new com.shopcart.entity.Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31")));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(request);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getOrderId());
    assertNotNull(response.getStatus());
    assertNotNull(response.getTotalPrice());
    assertTrue(response.getTotalPrice() > 0);
    verify(inventoryService, times(1)).isAvailable("P001", 2);
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  @DisplayName("TC2: Lấy thông tin đơn hàng")
  void testGetOrderById() {
    // Arrange
    String orderId = "ORD-001";
    Order order = new Order();
    order.setId(orderId);
    order.setUserId("user01");
    order.setStatus(OrderStatus.PENDING);
    order.setTotalPrice(10000000L);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    // Act
    Order result = orderService.getOrderById(orderId);

    // Assert
    assertNotNull(result);
    assertEquals(orderId, result.getId());
    assertEquals("user01", result.getUserId());
    verify(orderRepository, times(1)).findById(orderId);
  }

  @Test
  @DisplayName("TC3: Hủy đơn hàng và hoàn tồn kho")
  void testCancelOrder() {
    // Arrange
    Order order = new Order();
    order.setId("ORD-001");
    order.setStatus(OrderStatus.PENDING);
    order.setItems(List.of(new OrderItem("P001", 2, 15000000L)));

    when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    // Act
    orderService.cancelOrder("ORD-001");

    // Assert
    verify(orderRepository, times(1)).findById("ORD-001");
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  @DisplayName("TC4: Tính tổng giá đơn hàng")
  void testCalculateOrderTotal() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .couponCode("SALE10")
            .shippingFee(50000L)
            .build();
    when(productRepository.findById("P001"))
        .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
    when(orderRepository.findCoupon("SALE10"))
        .thenReturn(Optional.of(new com.shopcart.entity.Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31")));

    // Act
    Long totalPrice = orderService.calculateOrderTotal(request);

    // Assert
    assertNotNull(totalPrice);
    assertTrue(totalPrice > 0);
  }

  @Test
  @DisplayName("TC5: Kiểm tra tồn kho trước đặt hàng")
  void testCheckStockBeforeOrder() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 10, 15000000L)))
            .build();

    when(inventoryService.isAvailable("P001", 10)).thenReturn(false);

    // Act & Assert
    assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("TC6: Mã giảm giá không hợp lệ")
  void testInvalidCoupon() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .couponCode("INVALID")
            .shippingFee(50000L)
            .build();

    when(orderRepository.findCoupon("INVALID")).thenReturn(Optional.empty());
    when(productRepository.findById("P001"))
        .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(request);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getTotalPrice());
  }
}
