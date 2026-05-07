package com.shopcart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.service.CartService;

@DisplayName("Cart Controller Integration Tests")
@ExtendWith(MockitoExtension.class)
class CartControllerIntegrationTest {

  @Mock private CartService cartService;

  @InjectMocks private CartController cartController;

  @Test
  @DisplayName("TC1: Thêm sản phẩm vào giỏ hàng")
  void testAddToCart() {
    // Arrange
    CartItemRequest request = new CartItemRequest("P001", 2);
    CartItem mockCartItem = new CartItem("P001", 2, "Laptop Dell", 15000000L);
    
    when(cartService.addToCart(anyString(), any(CartItemRequest.class)))
        .thenReturn(mockCartItem);

    // Act
    ResponseEntity<?> response = cartController.addToCart("Bearer token123", request);
    
    // Assert
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(cartService, times(1)).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("TC2: Lấy giỏ hàng của người dùng")
  void testGetCart() {
    // Act & Assert
    ResponseEntity<?> response = cartController.getCart("Bearer token123", "user01");
    
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
  }

  @Test
  @DisplayName("TC3: Xóa sản phẩm khỏi giỏ hàng")
  void testRemoveFromCart() {
    // Act & Assert
    ResponseEntity<?> response = cartController.removeFromCart("Bearer token123", "user01", "P001");
    
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(cartService, times(1)).removeFromCart(anyString(), anyString());
  }
}
