package com.shopcart.security;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.controller.CartController;
import com.shopcart.controller.OrderController;
import com.shopcart.controller.ProductController;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.OrderStatus;
import com.shopcart.repository.ProductRepository;
import com.shopcart.service.CartService;
import com.shopcart.service.OrderService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Security Test Suite")
class SecurityTestSuite {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private OrderService orderService;

  @Mock private ProductRepository productRepository;

  @Mock private CartService cartService;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(
            new OrderController(orderService),
            new ProductController(productRepository),
            new CartController(cartService))
        .build();
  }

  @Test
  @DisplayName("ST_SQLI_001: SQL Injection payload must not crash order API")
  void testOrderSqlInjectionPayload() throws Exception {
    OrderRequest request = OrderRequest.builder()
        .userId("user01' OR '1'='1")
        .items(List.of(new OrderItemRequest("P001", 1, 1000000L)))
        .shippingAddress("123 Main St")
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(OrderResponse.builder().orderId("ORD-SEC-001").status(OrderStatus.PENDING).totalPrice(1000000L).build());

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("ST_XSS_001: XSS payload must not crash order API")
  void testOrderXssPayload() throws Exception {
    OrderRequest request = OrderRequest.builder()
        .userId("user01")
        .items(List.of(new OrderItemRequest("P001", 1, 1000000L)))
        .shippingAddress("<script>alert('xss')</script>")
        .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(OrderResponse.builder().orderId("ORD-SEC-002").status(OrderStatus.PENDING).totalPrice(1000000L).build());

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("ST_CSRF_001: No auth must be rejected for cart API")
  void testCartApiRejectsWhenMissingAuth() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();

    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("ST_IDOR_001: Order detail access requires auth header")
  void testOrderDetailRequiresAuth() throws Exception {
    mockMvc.perform(get("/api/order/{orderId}", "ORD-SEC-003")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("ST_HEADER_001: Product API requires authorization")
  void testProductApiRequiresAuth() throws Exception {
    mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
  }
}
