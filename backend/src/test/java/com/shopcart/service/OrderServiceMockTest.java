package com.shopcart.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.OrderStatus;
import com.shopcart.entity.Product;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;

@DisplayName("Order Service Mock Tests")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        .shippingAddress("123 Test Street, HCM")
        .paymentMethod("COD")
        .build();

    when(productRepository.findById("P001")).thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
    when(productRepository.findById("P002")).thenReturn(Optional.of(new Product("P002", "Mouse", 500000L, 20)));

    Coupon coupon = new Coupon("SALE10", "PERCENT", 10L, null, LocalDate.now().plusDays(5).toString());
    when(orderRepository.findCoupon("SALE10")).thenReturn(Optional.of(coupon));
  }

  @Test
  @DisplayName("Mock: Tạo order thành công")
  void testCreateOrderWithMock() {
    when(inventoryService.isAvailable(anyString(), anyInt())).thenReturn(true);
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order saved = invocation.getArgument(0);
      saved.setId("ORD-001");
      return saved;
    });

    OrderResponse response = orderService.createOrder(validRequest);

    assertEquals("ORD-001", response.getOrderId());
    assertEquals(OrderStatus.PENDING, response.getStatus());
    assertEquals(27500000L, response.getTotalPrice());

    verify(inventoryService, times(1)).isAvailable("P001", 2);
    verify(inventoryService, times(1)).isAvailable("P002", 1);
    verify(inventoryService, times(1)).decreaseStock("P001", 2);
    verify(inventoryService, times(1)).decreaseStock("P002", 1);
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Kiểm tra stock trước khi tạo order")
  void testCheckStockBeforeCreateOrder() {
    when(inventoryService.isAvailable("P001", 2)).thenReturn(false);

    assertThrows(InsufficientStockException.class, () -> orderService.createOrder(validRequest));

    verify(inventoryService, times(1)).isAvailable("P001", 2);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: Empty items list rejection")
  void testMockEmptyItemsRejection() {
    OrderRequest emptyRequest = OrderRequest.builder()
        .userId("user01")
        .items(List.of())
        .shippingFee(50000L)
        .build();

    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(emptyRequest));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Mock: calculateOrderTotal với fixed coupon")
  void testCalculateOrderTotalWithFixedCoupon() {
    when(orderRepository.findCoupon("FIXED100K"))
        .thenReturn(Optional.of(new Coupon("FIXED100K", "FIXED", 100000L, null, LocalDate.now().plusDays(5).toString())));

    OrderRequest fixedCouponRequest = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 1, 0L),
            new OrderItemRequest("P002", 1, 0L)))
        .couponCode("FIXED100K")
        .shippingFee(50000L)
        .build();

    long total = orderService.calculateOrderTotal(fixedCouponRequest);
    assertEquals(15450000L, total);
  }

  @Test
  @DisplayName("Mock: cancelOrder hoàn kho và cập nhật trạng thái")
  void testCancelOrder() {
    Order order = new Order();
    order.setId("ORD-CANCEL-001");
    order.setStatus(OrderStatus.PENDING);
    order.setItems(List.of(new OrderItem("P001", 2, 15000000L)));

    when(orderRepository.findById("ORD-CANCEL-001")).thenReturn(Optional.of(order));

    orderService.cancelOrder("ORD-CANCEL-001");

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    verify(inventoryService, times(1)).increaseStock("P001", 2);
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  @DisplayName("Mock: getOrderById trả về null nếu không tồn tại")
  void testGetOrderByIdNotFound() {
    when(orderRepository.findById("NOT_FOUND")).thenReturn(Optional.empty());

    Order result = orderService.getOrderById("NOT_FOUND");
    assertNull(result);

    when(orderRepository.findById("ORD-FOUND")).thenReturn(Optional.of(new Order()));
    assertNotNull(orderService.getOrderById("ORD-FOUND"));
  }
}
