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
            .items(
                List.of(
                    new OrderItemRequest("P001", 2, 15000000L),
                    new OrderItemRequest("P002", 1, 500000L)))
            .couponCode("SALE10")
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(inventoryService.isAvailable("P002", 1)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            inv -> {
              Order o = inv.getArgument(0);
              o.setId(UUID.randomUUID().toString());
              return o;
            });

    // Act
    OrderResponse response = orderService.createOrder(request);

    // Assert
    assertNotNull(response.getOrderId());
    assertEquals(OrderStatus.PENDING, response.getStatus());
    assertEquals(27950000L, response.getTotalPrice());
    verify(inventoryService, times(1)).decreaseStock("P001", 2);
    verify(inventoryService, times(1)).decreaseStock("P002", 1);
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

    OrderItem item1 = new OrderItem("P001", 2, 15000000L);
    OrderItem item2 = new OrderItem("P002", 1, 500000L);
    order.setItems(List.of(item1, item2));

    when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    // Act
    orderService.cancelOrder("ORD-001");

    // Assert
    verify(inventoryService, times(1)).increaseStock("P001", 2);
    verify(inventoryService, times(1)).increaseStock("P002", 1);
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  @DisplayName("TC4: Tính tổng giá đơn hàng chính xác")
  void testCalculateOrderTotal() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .couponCode("SALE10")
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // Act
    Long totalPrice = orderService.calculateOrderTotal(request);

    // Assert
    // Calculation: (15M * 2) * 0.9 + 50K = 27.050.000
    assertEquals(27050000L, totalPrice);
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

    // Mock coupon validation
    when(orderRepository.findCoupon("INVALID")).thenReturn(Optional.empty());

    // Act & Assert - should handle gracefully, create order without discount
    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    OrderResponse response = orderService.createOrder(request);
    assertNotNull(response);
    // Total = 30M + 50K = 30.050.000 (no discount)
    assertEquals(30050000L, response.getTotalPrice());
  }

  @Test
  @DisplayName("TC7: Sử dụng ArgumentCaptor để verify chi tiết Order")
  void testCreateOrderDetailedVerification() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .couponCode("SALE10")
            .shippingFee(50000L)
            .build();

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            inv -> {
              Order o = inv.getArgument(0);
              o.setId(UUID.randomUUID().toString());
              return o;
            });

    // Act
    orderService.createOrder(request);

    // Assert
    verify(orderRepository).save(orderCaptor.capture());
    Order capturedOrder = orderCaptor.getValue();

    assertEquals("user01", capturedOrder.getUserId());
    assertEquals(OrderStatus.PENDING, capturedOrder.getStatus());
    assertEquals(2, capturedOrder.getItems().size());
  }

  @Test
  @DisplayName("TC8: Cấu hình theo yêu cầu - Coupon giảm giá cố định")
  void testCreateOrderWithFixedDiscount() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 10000000L)))
            .couponCode("FIXED100K") // Fixed 100K discount
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 1)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            inv -> {
              Order o = inv.getArgument(0);
              o.setId(UUID.randomUUID().toString());
              return o;
            });

    // Act
    OrderResponse response = orderService.createOrder(request);

    // Assert
    // Total = 10M - 100K + 50K = 9.950.000
    assertEquals(9950000L, response.getTotalPrice());
  }
}
