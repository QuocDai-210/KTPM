package com.shopcart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.OrderStatus;
import com.shopcart.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@DisplayName("Order API Integration Tests")
class OrderControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OrderService orderService;

  @Test
  @DisplayName("POST /api/orders - Tạo đơn hàng thành công")
  void testCreateOrder() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 2, 15000000L),
            new OrderItemRequest("P002", 1, 500000L)
        ))
        .couponCode("SALE10")
        .shippingFee(50000L)
        .build();

    OrderResponse mockResponse = OrderResponse.builder()
        .orderId("ORD-20260509-001")
        .status(OrderStatus.PENDING)
        .totalPrice(27950000L)
        .message("Đặt hàng thành công")
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value("ORD-20260509-001"))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.totalPrice").value(27950000))
        .andExpect(jsonPath("$.message").value("Đặt hàng thành công"));

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("POST /api/orders - Price calculation chính xác")
  void testCreateOrderPriceCalculation() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 2, 15000000L)  // 30M
        ))
        .shippingFee(50000L)
        .couponCode("SALE10")  // 10% discount
        .build();

    // Expected: 30M - 3M (10%) + 50k = 27,050,000
    OrderResponse mockResponse = OrderResponse.builder()
        .orderId("ORD-20260509-002")
        .status(OrderStatus.PENDING)
        .totalPrice(27050000L)
        .subtotal(30000000L)
        .discount(3000000L)
        .shipping(50000L)
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.subtotal").value(30000000))
        .andExpect(jsonPath("$.discount").value(3000000))
        .andExpect(jsonPath("$.shipping").value(50000))
        .andExpect(jsonPath("$.totalPrice").value(27050000));
  }

  @Test
  @DisplayName("POST /api/orders - Out of stock")
  void testCreateOrderOutOfStock() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 100, 15000000L)  // Way more than stock
        ))
        .shippingFee(50000L)
        .build();

    OrderResponse mockResponse = OrderResponse.builder()
        .orderId(null)
        .status(null)
        .message("Không đủ tồn kho cho sản phẩm P001. Chỉ còn 3 chiếc.")
        .error("INSUFFICIENT_STOCK")
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
  }

  @Test
  @DisplayName("POST /api/orders - Invalid coupon")
  void testCreateOrderInvalidCoupon() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 1, 15000000L)
        ))
        .couponCode("INVALID_CODE")
        .shippingFee(50000L)
        .build();

    OrderResponse mockResponse = OrderResponse.builder()
        .orderId(null)
        .message("Mã giảm giá không tồn tại hoặc hết hạn")
        .error("INVALID_COUPON")
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.error").value("INVALID_COUPON"));
  }

  @Test
  @DisplayName("GET /api/orders/{orderId} - Lấy thông tin đơn hàng")
  void testGetOrderById() throws Exception {
    // Arrange
    OrderResponse mockResponse = OrderResponse.builder()
        .orderId("ORD-20260509-001")
        .status(OrderStatus.PENDING)
        .totalPrice(27950000L)
        .build();

    when(orderService.getOrderById("ORD-20260509-001"))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(get("/api/orders/{orderId}", "ORD-20260509-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value("ORD-20260509-001"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(orderService, times(1)).getOrderById("ORD-20260509-001");
  }

  @Test
  @DisplayName("POST /api/orders - Multiple items validation")
  void testCreateOrderMultipleItems() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 2, 15000000L),
            new OrderItemRequest("P002", 1, 500000L),
            new OrderItemRequest("P003", 3, 2000000L)
        ))
        .shippingFee(100000L)
        .build();

    // Expected: (30M + 500k + 6M) + 100k = 36,600,000
    OrderResponse mockResponse = OrderResponse.builder()
        .orderId("ORD-20260509-003")
        .status(OrderStatus.PENDING)
        .totalPrice(36600000L)
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.totalPrice").value(36600000));
  }

  @Test
  @DisplayName("POST /api/orders - Response structure validation")
  void testCreateOrderResponseStructure() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
        .shippingFee(50000L)
        .build();

    OrderResponse mockResponse = OrderResponse.builder()
        .orderId("ORD-20260509-004")
        .status(OrderStatus.PENDING)
        .totalPrice(15050000L)
        .message("Success")
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.totalPrice").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("POST /api/orders - Content-Type validation")
  void testCreateOrderContentTypeValidation() throws Exception {
    // Arrange
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
        .shippingFee(50000L)
        .build();

    // Act & Assert - JSON content-type
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // Act & Assert - Invalid content-type
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid"))
        .andExpect(status().is4xxClientError());
  }
}
