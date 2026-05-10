package com.shopcart.controller;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderStatus;
import com.shopcart.service.OrderService;

@WebMvcTest(OrderController.class)
@DisplayName("Order API Integration Tests")
class OrderControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private OrderService orderService;

  @Test
  @DisplayName("POST /api/orders - Tao don hang")
  void testCreateOrder() throws Exception {
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 2, 15000000L)))
            .shippingFee(50000L)
            .shippingAddress("123 Test Street, HCM")
            .paymentMethod("COD")
            .build();

    OrderResponse mockResponse =
        OrderResponse.builder()
            .orderId("ORD-001")
            .status(OrderStatus.PENDING)
            .totalPrice(30050000L)
            .build();

    when(orderService.createOrder(any(OrderRequest.class))).thenReturn(mockResponse);

    mockMvc
        .perform(
            post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value("ORD-001"))
        .andExpect(jsonPath("$.totalPrice").value(30050000));

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("GET /api/order/{orderId} - Lay thong tin don hang")
  void testGetOrderById() throws Exception {
    Order mockOrder = new Order();
    mockOrder.setId("ORD-001");
    mockOrder.setUserId("user01");
    mockOrder.setStatus(OrderStatus.PENDING);
    mockOrder.setTotalPrice(30050000L);

    when(orderService.getOrderForUser("ORD-001", "user01")).thenReturn(mockOrder);

    mockMvc
        .perform(get("/api/order/{orderId}", "ORD-001").header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("ORD-001"))
        .andExpect(jsonPath("$.totalPrice").value(30050000));

    verify(orderService, times(1)).getOrderForUser("ORD-001", "user01");
  }

  @Test
  @DisplayName("PATCH /api/order/{orderId}/cancel - Huy don hang")
  void testCancelOrder() throws Exception {
    mockMvc
        .perform(
            patch("/api/order/{orderId}/cancel", "ORD-001")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
        .andExpect(status().isOk());

    verify(orderService, times(1)).cancelOrderForUser("ORD-001", "user01");
  }

  @Test
  @DisplayName("POST /api/orders - Thieu Authorization")
  void testCreateOrderMissingAuthorization() throws Exception {
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
            .build();

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));

    verify(orderService, never()).createOrder(any(OrderRequest.class));
  }
}
