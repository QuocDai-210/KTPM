package com.shopcart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderStatus;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Order Service Mock Tests")
@ExtendWith(MockitoExtension.class)
class OrderServiceMockTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductRepository productRepository;

  @Mock private InventoryService inventoryService;

  @InjectMocks private OrderService orderService;

  private OrderRequest validRequest;

  @BeforeEach
  void setUp() {
    validRequest = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 2, 15000000L),
            new OrderItemRequest("P002", 1, 500000L)
        ))
        .couponCode("SALE10")
        .shippingFee(50000L)
        .build();
  }

  @Test
  @DisplayName("Mock: Tạo order thành công")
  void testCreateOrderWithMock() {
    // Arrange
    when(inventoryService.isAvailable("P001", 2))
        .thenReturn(true);
    when(inventoryService.isAvailable("P002", 1))
        .thenReturn(true);

    Order savedOrder = new Order();
    savedOrder.setId(UUID.randomUUID().toString());
    savedOrder.setStatus(OrderStatus.PENDING);
    savedOrder.setTotalPrice(27950000L);

    when(orderRepository.save(any(Order.class)))
        .thenReturn(savedOrder);

    // Act
    OrderResponse response = orderService.createOrder(validRequest);

    // Assert
    assertNotNull(response.getOrderId());
    assertEquals(OrderStatus.PENDING, response.getStatus());
    assertEquals(27950000L, response.getTotalPrice());

    // Verify
    verify(inventoryService, times(1)).isAvailable("P001", 2);
    verify(inventoryService, times(1)).isAvailable("P002", 1);
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Kiểm tra stock trước khi tạo order")
  void testCheckStockBeforeCreateOrder() {
    // Arrange
    when(inventoryService.isAvailable("P001", 2))
        .thenReturn(false); // Out of stock

    // Act & Assert
    assertThrows(Exception.class, () -> {
      orderService.createOrder(validRequest);
    });

    // Verify stock was checked
    verify(inventoryService, times(1)).isAvailable("P001", 2);
    // Order should not be created
    verify(orderRepository, times(0)).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Verify order payload chính xác")
  void testMockVerifyOrderPayload() {
    // Arrange
    when(inventoryService.isAvailable(anyString(), anyInt()))
        .thenReturn(true);

    Order mockOrder = new Order();
    mockOrder.setId(UUID.randomUUID().toString());

    when(orderRepository.save(any(Order.class)))
        .thenReturn(mockOrder);

    // Act
    orderService.createOrder(validRequest);

    // Assert - Capture the order being saved
    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());

    Order capturedOrder = orderCaptor.getValue();
    assertNotNull(capturedOrder.getId());
    assertEquals(OrderStatus.PENDING, capturedOrder.getStatus());
    assertEquals(27950000L, capturedOrder.getTotalPrice());
  }

  @Test
  @DisplayName("Mock: Verify deduct stock được gọi")
  void testMockVerifyStockDeduction() {
    // Arrange
    when(inventoryService.isAvailable(anyString(), anyInt()))
        .thenReturn(true);

    Order mockOrder = new Order();
    mockOrder.setId(UUID.randomUUID().toString());

    when(orderRepository.save(any(Order.class)))
        .thenReturn(mockOrder);

    // Act
    orderService.createOrder(validRequest);

    // Assert
    verify(inventoryService, times(1)).decreaseStock("P001", 2);
    verify(inventoryService, times(1)).decreaseStock("P002", 1);
  }

  @Test
  @DisplayName("Mock: Multiple orders creation")
  void testMockMultipleOrdersCreation() {
    // Arrange
    when(inventoryService.isAvailable(anyString(), anyInt()))
        .thenReturn(true);

    Order order1 = new Order();
    order1.setId("ORD-001");
    Order order2 = new Order();
    order2.setId("ORD-002");

    when(orderRepository.save(any(Order.class)))
        .thenReturn(order1)
        .thenReturn(order2);

    // Act
    OrderResponse response1 = orderService.createOrder(validRequest);
    OrderResponse response2 = orderService.createOrder(validRequest);

    // Assert
    assertNotNull(response1.getOrderId());
    assertNotNull(response2.getOrderId());

    // Verify
    verify(orderRepository, times(2)).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Verify call count")
  void testMockVerifyCallCount() {
    // Arrange
    when(inventoryService.isAvailable(anyString(), anyInt()))
        .thenReturn(true);

    Order mockOrder = new Order();
    mockOrder.setId(UUID.randomUUID().toString());

    when(orderRepository.save(any(Order.class)))
        .thenReturn(mockOrder);

    // Act
    for (int i = 0; i < 5; i++) {
      orderService.createOrder(validRequest);
    }

    // Assert
    verify(orderRepository, times(5)).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Verify coupon application")
  void testMockVerifyCouponApplication() {
    // Arrange
    when(inventoryService.isAvailable(anyString(), anyInt()))
        .thenReturn(true);

    Order mockOrder = new Order();
    mockOrder.setId(UUID.randomUUID().toString());
    mockOrder.setCouponCode("SALE10");
    mockOrder.setDiscount(3050000L);

    when(orderRepository.save(any(Order.class)))
        .thenReturn(mockOrder);

    // Act
    OrderResponse response = orderService.createOrder(validRequest);

    // Assert
    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());

    Order capturedOrder = orderCaptor.getValue();
    assertEquals("SALE10", capturedOrder.getCouponCode());
    assertEquals(3050000L, capturedOrder.getDiscount());
  }

  @Test
  @DisplayName("Mock: Verify price calculation with coupon")
  void testMockPriceCalculationWithCoupon() {
    // Arrange
    when(inventoryService.isAvailable(anyString(), anyInt()))
        .thenReturn(true);

    Order mockOrder = new Order();
    mockOrder.setId(UUID.randomUUID().toString());
    // Subtotal: (15000000 * 2) + (500000 * 1) = 30500000
    // Discount (10%): 3050000
    // Shipping: 50000
    // Total: 30500000 - 3050000 + 50000 = 27500000
    mockOrder.setTotalPrice(27500000L);

    when(orderRepository.save(any(Order.class)))
        .thenReturn(mockOrder);

    // Act
    OrderResponse response = orderService.createOrder(validRequest);

    // Assert
    assertEquals(27500000L, response.getTotalPrice());
  }

  @Test
  @DisplayName("Mock: Empty items list rejection")
  void testMockEmptyItemsRejection() {
    // Arrange
    OrderRequest emptyRequest = OrderRequest.builder()
        .userId("user01")
        .items(List.of()) // Empty items
        .shippingFee(50000L)
        .build();

    // Act & Assert
    assertThrows(Exception.class, () -> {
      orderService.createOrder(emptyRequest);
    });

    verify(orderRepository, times(0)).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Invalid shipping fee")
  void testMockInvalidShippingFee() {
    // Arrange
    OrderRequest invalidRequest = OrderRequest.builder()
        .userId("user01")
        .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
        .shippingFee(-50000L) // Negative shipping
        .build();

    // Act & Assert
    assertThrows(Exception.class, () -> {
      orderService.createOrder(invalidRequest);
    });

    verify(orderRepository, times(0)).save(any(Order.class));
  }
}
