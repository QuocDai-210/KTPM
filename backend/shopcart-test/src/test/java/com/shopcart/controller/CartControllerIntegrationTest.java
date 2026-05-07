package com.shopcart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@DisplayName("Cart API Integration Tests")
class CartControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private CartService cartService;

  @Test
  @DisplayName("TC1: POST /api/cart/add - Thêm sản phẩm vào giỏ hàng")
  void testAddToCart() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(2)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Thêm vào giỏ hàng thành công")
        .cartTotal(30000000L)
        .build();

    when(cartService.addToCart(anyString(), any()))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc.perform(post("/api/cart/add")
        .header("Authorization", "Bearer token123")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Thêm vào giỏ hàng thành công"))
        .andExpect(jsonPath("$.cartTotal").value(30000000));
  }

  @Test
  @DisplayName("TC2: GET /api/cart/{userId} - Lấy giỏ hàng của người dùng")
  void testGetCart() throws Exception {
    // Arrange
    String userId = "user01";
    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .items(2)
        .cartTotal(30500000L)
        .build();

    when(cartService.getCart(userId)).thenReturn(mockResponse);

    // Act & Assert
    mockMvc.perform(get("/api/cart/{userId}", userId)
        .header("Authorization", "Bearer token123")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.items").value(2))
        .andExpect(jsonPath("$.cartTotal").value(30500000));
  }

  @Test
  @DisplayName("TC3: DELETE /api/cart/{userId}/{productId} - Xóa sản phẩm khỏi giỏ")
  void testRemoveFromCart() throws Exception {
    // Arrange
    String userId = "user01";
    String productId = "P001";

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Sản phẩm đã được xóa")
        .cartTotal(15500000L)
        .build();

    when(cartService.removeFromCart(userId, productId)).thenReturn(mockResponse);

    // Act & Assert
    mockMvc.perform(delete("/api/cart/{userId}/{productId}", userId, productId)
        .header("Authorization", "Bearer token123")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Sản phẩm đã được xóa"));
  }

  @Test
  @DisplayName("TC4: PUT /api/cart/update - Cập nhật số lượng sản phẩm")
  void testUpdateQuantity() throws Exception {
    // Arrange
    UpdateQuantityRequest request = UpdateQuantityRequest.builder()
        .userId("user01")
        .productId("P001")
        .quantity(5)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Cập nhật số lượng thành công")
        .cartTotal(75000000L)
        .build();

    when(cartService.updateQuantity(anyString(), anyString(), anyInt()))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc.perform(put("/api/cart/update")
        .header("Authorization", "Bearer token123")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.cartTotal").value(75000000));
  }

  @Test
  @DisplayName("TC5: POST /api/cart/add - Xử lý lỗi khi số lượng vượt tồn kho")
  void testAddToCartInsufficientStock() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(15)
        .build();

    when(cartService.addToCart(anyString(), any()))
        .thenThrow(new InsufficientStockException("Tồn kho không đủ"));

    // Act & Assert
    mockMvc.perform(post("/api/cart/add")
        .header("Authorization", "Bearer token123")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").containsString("tồn kho"));
  }

  @Test
  @DisplayName("TC6: POST /api/cart/add - Unauthorized request")
  void testAddToCartUnauthorized() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(2)
        .build();

    // Act & Assert
    mockMvc.perform(post("/api/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("TC7: POST /api/cart/add - Invalid request body")
  void testAddToCartInvalidBody() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/api/cart/add")
        .header("Authorization", "Bearer token123")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest());
  }
}
