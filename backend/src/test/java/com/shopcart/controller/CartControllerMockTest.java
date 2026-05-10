package com.shopcart.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.common.ApiMessages;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.dto.UpdateQuantityRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.service.CartService;

@WebMvcTest(CartController.class)
@DisplayName("Cart Controller Mock Tests")
class CartControllerMockTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private CartService cartService;

  @Test
  @DisplayName("POST /api/cart/add - Them san pham")
  void testAddToCartWithMockedService() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(2).build();
    CartItem mockItem = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);
    CartResponse mockResponse =
        CartResponse.builder()
            .success(true)
            .message(ApiMessages.CART_ADD_SUCCESS)
            .cartTotal(30000000L)
            .build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class))).thenReturn(mockItem);
    when(cartService.getCartByUser(anyString())).thenReturn(List.of(mockItem));

    mockMvc
        .perform(
            post("/api/cart/add")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(mockResponse.isSuccess()))
        .andExpect(jsonPath("$.message").value(mockResponse.getMessage()))
        .andExpect(jsonPath("$.cartTotal").value(mockResponse.getCartTotal()));

    verify(cartService, times(1)).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock: Mock service tra ve loi het hang")
  void testAddToCartWithMockedServiceFailure() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(99).build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenThrow(new InsufficientStockException(ApiMessages.INSUFFICIENT_STOCK));

    mockMvc
        .perform(
            post("/api/cart/add")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value(ApiMessages.INSUFFICIENT_STOCK));

    verify(cartService, times(1)).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock Thieu Authorization thi khong goi service")
  void testAddToCartUnauthorized() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();

    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());

    verify(cartService, never()).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock Verify so lan goi mock service")
  void testMockVerifyCallCount() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();
    CartItem mockItem = new CartItem("P001", "user01", 1, "Laptop Dell", 15000000L);

    when(cartService.addToCart(anyString(), any(CartItemRequest.class))).thenReturn(mockItem);
    when(cartService.getCartByUser(anyString())).thenReturn(List.of(mockItem));

    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post("/api/cart/add")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    verify(cartService, times(3)).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock Token khong hop le thi khong goi service")
  void testAddToCartInvalidToken() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();

    mockMvc
        .perform(
            post("/api/cart/add")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());

    verify(cartService, never()).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock Khong cho cap nhat gio hang cua user khac")
  void testUpdateQuantityForbiddenForAnotherUser() throws Exception {
    UpdateQuantityRequest request = new UpdateQuantityRequest("user01", "P001", 2);

    mockMvc
        .perform(
            put("/api/cart/update")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-user02")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));

    verify(cartService, never()).updateQuantity(anyString(), anyString(), anyInt());
  }
}
