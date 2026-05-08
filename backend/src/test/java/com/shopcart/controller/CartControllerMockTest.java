package com.shopcart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@DisplayName("Cart Controller Mock Tests")
class CartControllerMockTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CartService cartService;

  @Test
  @DisplayName("Mock: Thêm sản phẩm vào giỏ hàng - Success")
  void testAddToCartWithMockedService() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(2)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Thêm vào giỏ hàng thành công")
        .cartTotal(30000000L)
        .cartCount(2)
        .build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/cart/add")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Thêm vào giỏ hàng thành công"))
        .andExpect(jsonPath("$.cartTotal").value(30000000))
        .andExpect(jsonPath("$.cartCount").value(2));

    // Verify
    verify(cartService, times(1))
        .addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock: Thêm sản phẩm thất bại - Stock không đủ")
  void testAddToCartFailureWithMock() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(50)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(false)
        .message("Số lượng không được vượt quá tồn kho")
        .error("INSUFFICIENT_STOCK")
        .build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").contains("vượt quá tồn kho"));
  }

  @Test
  @DisplayName("Mock: Xác minh service được gọi với đúng arguments")
  void testMockVerifyCallArguments() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P002")
        .quantity(1)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Success")
        .cartTotal(500000L)
        .cartCount(1)
        .build();

    when(cartService.addToCart("user01", request))
        .thenReturn(mockResponse);

    // Act
    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Assert - Verify arguments
    verify(cartService, times(1))
        .addToCart(argThat(arg -> arg.equals("user01") || arg != null),
            argThat(req -> req.getProductId().equals("P002") && req.getQuantity() == 1));
  }

  @Test
  @DisplayName("Mock: Service được gọi bao nhiêu lần")
  void testMockVerifyCallCount() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(1)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Success")
        .build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenReturn(mockResponse);

    // Act
    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post("/api/cart/add")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    // Assert
    verify(cartService, times(3))
        .addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("Mock: Response structure validation")
  void testMockResponseStructure() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(2)
        .build();

    CartResponse mockResponse = CartResponse.builder()
        .success(true)
        .message("Thêm vào giỏ hàng thành công")
        .cartTotal(30000000L)
        .cartCount(2)
        .items(java.util.List.of())
        .build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").exists())
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.cartTotal").exists())
        .andExpect(jsonPath("$.cartCount").exists())
        .andExpect(jsonPath("$.items").isArray());
  }

  @Test
  @DisplayName("Mock: Multiple invocations with different responses")
  void testMockMultipleInvocations() throws Exception {
    // Arrange
    CartItemRequest request = CartItemRequest.builder()
        .productId("P001")
        .quantity(1)
        .build();

    CartResponse response1 = CartResponse.builder()
        .success(true)
        .cartCount(1)
        .cartTotal(15000000L)
        .build();

    CartResponse response2 = CartResponse.builder()
        .success(true)
        .cartCount(2)
        .cartTotal(30000000L)
        .build();

    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenReturn(response1)
        .thenReturn(response2);

    // Act & Assert - First call
    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cartCount").value(1));

    // Act & Assert - Second call
    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cartCount").value(2));
  }
}
