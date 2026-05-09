package com.shopcart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order API Integration Tests")
class OrderControllerIntegrationTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private OrderService orderService;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new OrderController(orderService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("POST /api/orders - Táº¡o Ä‘Æ¡n hĂ ng thĂ nh cĂ´ng")
  void testCreateOrder() throws Exception {
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(
                List.of(
                    new OrderItemRequest("P001", 2, 15000000L),
                    new OrderItemRequest("P002", 1, 500000L)))
            .shippingFee(50000L)
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
  @DisplayName("POST /api/orders - Thiáº¿u Authorization tráº£ vá» 403")
  void testCreateOrderUnauthorized() throws Exception {
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
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/order/{orderId} - Láº¥y thĂ´ng tin Ä‘Æ¡n hĂ ng")
  void testGetOrderById() throws Exception {
    Order mockResponse = new Order();
    mockResponse.setId("ORD-20260509-001");
    mockResponse.setUserId("user01");
    mockResponse.setStatus(OrderStatus.PENDING);
    mockResponse.setTotalPrice(27950000L);

    when(orderService.getOrderForUser("ORD-20260509-001", "user01")).thenReturn(mockResponse);

    mockMvc
        .perform(get("/api/order/{orderId}", "ORD-20260509-001").header("Authorization", "Bearer token123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("ORD-20260509-001"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(orderService, times(1)).getOrderForUser("ORD-20260509-001", "user01");
  }

  @Test
  @DisplayName("PATCH /api/order/{orderId}/cancel - Há»§y Ä‘Æ¡n thĂ nh cĂ´ng")
  void testCancelOrder() throws Exception {
    mockMvc
        .perform(patch("/api/order/{orderId}/cancel", "ORD-20260509-001").header("Authorization", "Bearer token123"))
        .andExpect(status().isOk());

    verify(orderService, times(1)).cancelOrderForUser("ORD-20260509-001", "user01");
  }

  @Test
  @DisplayName("GET /api/order/{orderId} - Sai user tráº£ vá» 403")
  void testGetOrderForbidden() throws Exception {
    when(orderService.getOrderForUser("ORD-20260509-001", "user02"))
        .thenThrow(new AccessDeniedException("KhĂ´ng Ä‘Æ°á»£c truy cáº­p"));

    mockMvc
        .perform(get("/api/order/{orderId}", "ORD-20260509-001").header("Authorization", "Bearer token-user02"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("POST /api/orders - invalid token tráº£ vá» 403")
  void testCreateOrderInvalidToken() throws Exception {
    OrderRequest request =
        OrderRequest.builder().items(List.of(new OrderItemRequest("P001", 1, 15000000L))).build();

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}
