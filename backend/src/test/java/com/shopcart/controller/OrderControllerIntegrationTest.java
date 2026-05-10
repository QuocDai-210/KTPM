package com.shopcart.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

  @Nested
  @DisplayName("a) Test POST /api/orders (Tao don hang)")
  class CreateOrderEndpointTests {

    @Test
    @DisplayName("POST /api/orders - Vi du minh hoa tao don hang thanh cong")
    void testCreateOrderSuccessExample() throws Exception {
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

      OrderResponse mockResponse =
          OrderResponse.builder()
              .orderId("ORD-20260509-001")
              .status(OrderStatus.PENDING)
              .totalPrice(27950000L)
              .build();

      when(orderService.createOrder(any(OrderRequest.class))).thenReturn(mockResponse);

      mockMvc
          .perform(
              post("/api/orders")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.orderId").value("ORD-20260509-001"))
          .andExpect(jsonPath("$.status").value("PENDING"))
          .andExpect(jsonPath("$.totalPrice").value(27950000));

      verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("POST /api/orders - Tu dong gan userId tu token khi request khong co userId")
    void testCreateOrderUsesAuthenticatedUserWhenUserIdMissing() throws Exception {
      OrderRequest request =
          OrderRequest.builder()
              .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
              .shippingFee(50000L)
              .shippingAddress("123 Test Street, HCM")
              .paymentMethod("COD")
              .build();

      OrderResponse mockResponse =
          OrderResponse.builder()
              .orderId("ORD-20260510-002")
              .status(OrderStatus.PENDING)
              .totalPrice(15050000L)
              .build();

      when(orderService.createOrder(any(OrderRequest.class))).thenReturn(mockResponse);

      mockMvc
          .perform(
              post("/api/orders")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.orderId").value("ORD-20260510-002"))
          .andExpect(jsonPath("$.status").value("PENDING"))
          .andExpect(jsonPath("$.totalPrice").value(15050000));

      verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }
  }

  @Nested
  @DisplayName("b) Test endpoint bat ky trong nhom Order")
  class AdditionalOrderEndpointTests {

    @Test
    @DisplayName("GET /api/order/{orderId} - Lay thong tin don hang")
    void testGetOrderById() throws Exception {
      Order mockOrder = new Order();
      mockOrder.setId("ORD-20260510-001");
      mockOrder.setUserId("user01");
      mockOrder.setStatus(OrderStatus.PENDING);
      mockOrder.setTotalPrice(27950000L);

      when(orderService.getOrderForUser("ORD-20260510-001", "user01")).thenReturn(mockOrder);

      mockMvc
          .perform(
              get("/api/order/{orderId}", "ORD-20260510-001")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value("ORD-20260510-001"))
          .andExpect(jsonPath("$.userId").value("user01"))
          .andExpect(jsonPath("$.status").value("PENDING"))
          .andExpect(jsonPath("$.totalPrice").value(27950000));

      verify(orderService, times(1)).getOrderForUser("ORD-20260510-001", "user01");
    }
  }

  @Nested
  @DisplayName("c) Cac endpoint con lai")
  class RemainingEndpointAndStatusCodeTests {

    @Test
    @DisplayName("PATCH /api/order/{orderId}/cancel - Huy don hang thanh cong")
    void testCancelOrder() throws Exception {
      mockMvc
          .perform(
              patch("/api/order/{orderId}/cancel", "ORD-20260510-001")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
          .andExpect(status().isOk());

      verify(orderService, times(1)).cancelOrderForUser("ORD-20260510-001", "user01");
    }

    @Test
    @DisplayName("POST /api/orders - Thieu Authorization tra ve 403")
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

    @Test
    @DisplayName("POST /api/orders - Invalid token tra ve 403")
    void testCreateOrderInvalidToken() throws Exception {
      OrderRequest request =
          OrderRequest.builder()
              .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
              .build();

      mockMvc
          .perform(
              post("/api/orders")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.success").value(false));

      verify(orderService, never()).createOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("POST /api/orders - Khong cho user dat hang thay user khac")
    void testCreateOrderForbiddenForAnotherUser() throws Exception {
      OrderRequest request =
          OrderRequest.builder()
              .userId("user02")
              .items(List.of(new OrderItemRequest("P001", 1, 15000000L)))
              .build();

      mockMvc
          .perform(
              post("/api/orders")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.success").value(false));

      verify(orderService, never()).createOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("GET /api/order/{orderId} - Sai user tra ve 403")
    void testGetOrderForbiddenForAnotherUser() throws Exception {
      when(orderService.getOrderForUser("ORD-20260510-001", "user02"))
          .thenThrow(new AccessDeniedException("Khong duoc truy cap don hang"));

      mockMvc
          .perform(
              get("/api/order/{orderId}", "ORD-20260510-001")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token-user02"))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Khong duoc truy cap don hang"));
    }

    @Test
    @DisplayName("GET /api/order/{orderId} - Don hang khong ton tai tra ve 404")
    void testGetOrderNotFound() throws Exception {
      when(orderService.getOrderForUser("ORD-NOT-FOUND", "user01"))
          .thenThrow(new NoSuchElementException("Order not found"));

      mockMvc
          .perform(
              get("/api/order/{orderId}", "ORD-NOT-FOUND")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("Order not found"));
    }
  }
}
