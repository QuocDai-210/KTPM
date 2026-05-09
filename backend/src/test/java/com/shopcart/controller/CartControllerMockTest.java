package com.shopcart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.UpdateQuantityRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.service.CartService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Controller Mock Tests")
class CartControllerMockTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CartService cartService;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CartController(cartService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("Mock: ThĂªm sáº£n pháº©m vĂ o giá» hĂ ng - Success")
  void testAddToCartWithMockedService() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(2).build();
    CartItem mockItem = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);

    when(cartService.addToCart(anyString(), any(CartItemRequest.class))).thenReturn(mockItem);
    when(cartService.getCartByUser(anyString())).thenReturn(List.of(mockItem));

    mockMvc
        .perform(
            post("/api/cart/add")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Added item to cart successfully"))
        .andExpect(jsonPath("$.cartTotal").value(30000000))
        .andExpect(jsonPath("$.itemCount").value(2));

    verify(cartService, times(1)).addToCart(anyString(), any(CartItemRequest.class));
    verify(cartService, times(1)).getCartByUser(anyString());
  }

  @Test
  @DisplayName("Mock: Thiáº¿u Authorization tráº£ vá» 401")
  void testAddToCartUnauthorized() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();

    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Mock: Gá»i API 3 láº§n thĂ¬ service addToCart Ä‘Æ°á»£c gá»i 3 láº§n")
  void testMockVerifyCallCount() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();
    CartItem mockItem = new CartItem("P001", "user01", 1, "Laptop Dell", 15000000L);

    when(cartService.addToCart(anyString(), any(CartItemRequest.class))).thenReturn(mockItem);
    when(cartService.getCartByUser(anyString())).thenReturn(List.of(mockItem));

    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post("/api/cart/add")
                  .header("Authorization", "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    verify(cartService, times(3)).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock: Token khĂ´ng há»£p lá»‡ tráº£ vá» 401")
  void testAddToCartInvalidToken() throws Exception {
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
  @DisplayName("Mock: KhĂ´ng cho cáº­p nháº­t giỏ hĂ ng cá»§a user khĂ¡c")
  void testUpdateQuantityForbiddenForAnotherUser() throws Exception {
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
}
