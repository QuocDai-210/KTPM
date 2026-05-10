package com.shopcart.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.controller.CartController;
import com.shopcart.controller.GlobalExceptionHandler;
import com.shopcart.controller.OrderController;
import com.shopcart.controller.ProductController;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.dto.UpdateQuantityRequest;
import com.shopcart.entity.OrderStatus;
import com.shopcart.repository.ProductRepository;
import com.shopcart.service.CartService;
import com.shopcart.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new OrderController(orderService),
                new ProductController(productRepository),
                new CartController(cartService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("ST_SQLI_001: SQL Injection payload must be rejected as invalid product")
  void testOrderSqlInjectionPayload() throws Exception {
    String sqlInjectionProductId = "P001' OR '1'='1";
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest(sqlInjectionProductId, 1, 1000000L)))
            .shippingAddress("123 Main St")
            .paymentMethod("COD")
            .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenThrow(new IllegalArgumentException("Product ID khong hop le"));

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verify(orderService, times(1)).createOrder(any(OrderRequest.class));
  }

  @Test
  @DisplayName("ST_XSS_001: XSS payload must not crash order API")
  void testOrderXssPayload() throws Exception {
    OrderRequest request =
        OrderRequest.builder()
            .userId("user01")
            .items(List.of(new OrderItemRequest("P001", 1, 1000000L)))
            .shippingAddress("<script>alert('xss')</script>")
            .paymentMethod("COD")
            .build();

    when(orderService.createOrder(any(OrderRequest.class)))
        .thenReturn(
            OrderResponse.builder()
                .orderId("ORD-SEC-002")
                .status(OrderStatus.PENDING)
                .totalPrice(1000000L)
                .build());

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    ArgumentCaptor<OrderRequest> captor = ArgumentCaptor.forClass(OrderRequest.class);
    verify(orderService, times(1)).createOrder(captor.capture());
    org.junit.jupiter.api.Assertions.assertEquals(
        "<script>alert('xss')</script>", captor.getValue().getShippingAddress());
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
    mockMvc.perform(get("/api/order/{orderId}", "ORD-SEC-003")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("ST_HEADER_001: Product API requires authorization")
  void testProductApiRequiresAuth() throws Exception {
    mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("ST_AUTH_002: Invalid token must be rejected")
  void testCartApiRejectsInvalidToken() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();

    mockMvc
        .perform(
            post("/api/cart/add")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("ST_IDOR_002: Cart update for another user must be forbidden")
  void testCartUpdateForAnotherUserForbidden() throws Exception {
    UpdateQuantityRequest request = new UpdateQuantityRequest("user01", "P001", 2);

    mockMvc
        .perform(
            put("/api/cart/update")
                .header("Authorization", "Bearer token-user02")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("ST_IDOR_003: Order for another user must be forbidden")
  void testOrderForAnotherUserForbidden() throws Exception {
    when(orderService.getOrderForUser("ORD-SEC-004", "user02"))
        .thenThrow(new AccessDeniedException("forbidden"));

    mockMvc
        .perform(get("/api/order/{orderId}", "ORD-SEC-004").header("Authorization", "Bearer token-user02"))
        .andExpect(status().isForbidden());
  }
}
