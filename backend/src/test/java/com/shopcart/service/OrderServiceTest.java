package com.shopcart.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductRepository productRepository;

  @Mock private InventoryService inventoryService;

  @Mock private CartRepository cartRepository;

  @InjectMocks private OrderService orderService;

  @BeforeEach
  void setUpCatalogDefaults() {
    when(productRepository.findById("P001"))
        .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
    when(productRepository.findById("P002"))
        .thenReturn(Optional.of(new Product("P002", "Mouse Logitech", 500000L, 50)));
    when(productRepository.findById("P003"))
        .thenReturn(Optional.of(new Product("P003", "Keyboard Mechanical", 2000000L, 10)));
    when(orderRepository.findCoupon("SALE10"))
        .thenReturn(Optional.of(new com.shopcart.entity.Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31")));
    when(orderRepository.findCoupon("SAVE500"))
        .thenReturn(Optional.of(new com.shopcart.entity.Coupon("SAVE500", "FIXED", 500000L, 0L, "2026-12-31")));
  }

  private OrderRequest withCheckoutInfo(OrderRequest request) {
    request.setShippingAddress("123 Test Street, HCM");
    request.setPaymentMethod("COD");
    return request;
  }

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
    OrderResponse response = orderService.createOrder(withCheckoutInfo(request));

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
    assertThrows(InsufficientStockException.class, () -> orderService.createOrder(withCheckoutInfo(request)));
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

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(withCheckoutInfo(request)));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("TC7: Đơn hàng với nhiều sản phẩm")
  void testCreateOrderMultipleItems() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(
                new OrderItemRequest("P001", 2, 15000000L),
                new OrderItemRequest("P002", 1, 500000L),
                new OrderItemRequest("P003", 3, 2000000L)
            ))
            .shippingFee(100000L)
            .build();

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(inventoryService.isAvailable("P002", 1)).thenReturn(true);
    when(inventoryService.isAvailable("P003", 3)).thenReturn(true);

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(withCheckoutInfo(request));

    // Assert
    assertNotNull(response.getOrderId());
    assertNotNull(response.getTotalPrice());
    assertTrue(response.getTotalPrice() > 0);
  }

  @Test
  @DisplayName("TC8: Áp dụng coupon % discount")
  void testApplyPercentCoupon() {
    // Arrange - Subtotal: 30M, 10% = 3M discount, Total: 27M
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .couponCode("SALE10")
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 2)).thenReturn(true);
    when(orderRepository.findCoupon("SALE10"))
        .thenReturn(Optional.of(new com.shopcart.entity.Coupon("SALE10", "PERCENT", 10L, 0L, "2026-12-31")));

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(withCheckoutInfo(request));

    // Assert
    assertNotNull(response);
    assertTrue(response.getTotalPrice() > 0);
    // Total should be: (30M - 3M) + 50k = 27.05M
  }

  @Test
  @DisplayName("TC9: Áp dụng coupon fixed amount")
  void testApplyFixedCoupon() {
    // Arrange - Subtotal: 15M, Fixed: 500k discount, Total: 14.55M
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .couponCode("SAVE500")
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 1)).thenReturn(true);
    when(orderRepository.findCoupon("SAVE500"))
        .thenReturn(Optional.of(new com.shopcart.entity.Coupon("SAVE500", "FIXED", 0L, 500000L, "2026-12-31")));

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(withCheckoutInfo(request));

    // Assert
    assertNotNull(response);
    assertTrue(response.getTotalPrice() > 0);
  }

  @Test
  @DisplayName("TC10: Kiểm tra giảm tồn kho sau tạo order")
  void testDecreaseInventoryAfterOrder() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(
                new OrderItemRequest("P001", 2, 15000000L),
                new OrderItemRequest("P002", 1, 500000L)
            ))
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable(anyString(), anyInt())).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    orderService.createOrder(withCheckoutInfo(request));

    // Assert
    verify(inventoryService, times(1)).decreaseStock("P001", 2);
    verify(inventoryService, times(1)).decreaseStock("P002", 1);
  }

  @Test
  @DisplayName("TC11: Empty items list rejection")
  void testCreateOrderEmptyItems() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of()) // Empty
            .shippingFee(50000L)
            .build();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(withCheckoutInfo(request)));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("TC12: Negative shipping fee rejection")
  void testCreateOrderNegativeShipping() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .shippingFee(-50000L) // Negative
            .build();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(withCheckoutInfo(request)));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("TC13: Verify order status is PENDING after creation")
  void testOrderStatusPending() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 1)).thenReturn(true);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          o.setStatus(OrderStatus.PENDING);
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(withCheckoutInfo(request));

    // Assert
    assertEquals(OrderStatus.PENDING, response.getStatus());
  }

  @Test
  @DisplayName("TC14: Verify order ID is generated")
  void testOrderIdGeneration() {
    // Arrange
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .shippingFee(50000L)
            .build();

    when(inventoryService.isAvailable("P001", 1)).thenReturn(true);
    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    when(orderRepository.save(orderCaptor.capture()))
        .thenAnswer(inv -> {
          Order o = inv.getArgument(0);
          o.setId(UUID.randomUUID().toString());
          return o;
        });

    // Act
    OrderResponse response = orderService.createOrder(withCheckoutInfo(request));

    // Assert
    assertNotNull(response.getOrderId());
  }

  @Test
  @DisplayName("TC15: Missing shipping address rejection")
  void testMissingShippingAddress() {
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .shippingFee(50000L)
            .paymentMethod("COD")
            .build();

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
    assertEquals("Địa chỉ giao hàng không được để trống", ex.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("TC16: Missing payment method rejection")
  void testMissingPaymentMethod() {
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .shippingFee(50000L)
            .shippingAddress("123 Test Street, HCM")
            .build();

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
    assertEquals("Phương thức thanh toán không được để trống", ex.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }
}
