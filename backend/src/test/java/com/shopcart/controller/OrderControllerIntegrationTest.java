package com.shopcart.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderStatus;
import com.shopcart.service.OrderService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order API Integration Tests")
class OrderControllerIntegrationTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private OrderService orderService;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService)).build();
  }

  @Test
  @DisplayName("POST /api/orders - Tạo đơn hàng thành công")
  void testCreateOrder() throws Exception {
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(
            new OrderItemRequest("P001", 2, 15000000L),
            new OrderItemRequest("P002", 1, 500000L)
        ))
        .shippingFee(50000L)
        .build();

    OrderResponse mockResponse = OrderResponse.builder()
        .orderId("ORD-20260509-001")
        .status(OrderStatus.PENDING)
        .totalPrice(27950000L)
        .build();

    when(orderService.createOrder(any(OrderRequest.class))).thenReturn(mockResponse);

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value("ORD-20260509-001"))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.totalPrice").value(27950000));

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("POST /api/orders - Thiếu Authorization trả về 401")
  void testCreateOrderUnauthorized() throws Exception {
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
        .build();

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /api/order/{orderId} - Lấy thông tin đơn hàng")
  void testGetOrderById() throws Exception {
        Order mockResponse = new Order();
        mockResponse.setId("ORD-20260509-001");
        mockResponse.setStatus(OrderStatus.PENDING);
        mockResponse.setTotalPrice(27950000L);

    when(orderService.getOrderById("ORD-20260509-001")).thenReturn(mockResponse);

    mockMvc
        .perform(
            get("/api/order/{orderId}", "ORD-20260509-001")
                .header("Authorization", "Bearer token123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("ORD-20260509-001"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(orderService, times(1)).getOrderById("ORD-20260509-001");
  }

  @Test
  @DisplayName("PATCH /api/order/{orderId}/cancel - Hủy đơn thành công")
  void testCancelOrder() throws Exception {
    mockMvc
        .perform(
            patch("/api/order/{orderId}/cancel", "ORD-20260509-001")
                .header("Authorization", "Bearer token123"))
        .andExpect(status().isOk());

    verify(orderService, times(1)).cancelOrder("ORD-20260509-001");
  }
}
