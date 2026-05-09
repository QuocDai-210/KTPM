package com.shopcart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.dto.UpdateQuantityRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.service.CartService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

@DisplayName("Cart Controller Integration Tests")
@ExtendWith(MockitoExtension.class)
class CartControllerIntegrationTest {

  @Mock private CartService cartService;

  @InjectMocks private CartController cartController;

  @Test
  @DisplayName("TC1: ThĂªm sáº£n pháº©m vĂ o giá» hĂ ng")
  void testAddToCart() {
    CartItemRequest request = new CartItemRequest("P001", 2);
    CartItem mockCartItem = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);

    when(cartService.addToCart("user01", request)).thenReturn(mockCartItem);
    when(cartService.getCartByUser("user01")).thenReturn(List.of(mockCartItem));

    ResponseEntity<?> response = cartController.addToCart("Bearer token123", request);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(cartService, times(1)).addToCart("user01", request);
    verify(cartService, times(1)).getCartByUser("user01");
  }

  @Test
  @DisplayName("TC2: Láº¥y giá» hĂ ng cá»§a ngÆ°á»i dĂ¹ng")
  void testGetCart() {
    when(cartService.getCartByUser("user01")).thenReturn(List.of());

    ResponseEntity<?> response = cartController.getCart("Bearer token123", "user01");

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
  }

  @Test
  @DisplayName("TC3: XĂ³a sáº£n pháº©m khá»i giá» hĂ ng")
  void testRemoveFromCart() {
    when(cartService.getCartByUser("user01")).thenReturn(List.of());

    ResponseEntity<?> response =
        cartController.removeFromCart("Bearer token123", "user01", "P001");

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(cartService, times(1)).removeFromCart("user01", "P001");
  }

  @Test
  @DisplayName("TC4: Token khĂ´ng há»£p lá»‡ tráº£ vá» 401")
  void testAddToCartInvalidToken() {
    ResponseEntity<?> response =
        cartController.addToCart("Bearer invalid-token", new CartItemRequest("P001", 1));

    assertEquals(401, response.getStatusCode().value());
    verify(cartService, never()).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("TC5: KhĂ´ng cho truy cáº­p giá» hĂ ng ngÆ°á»i khĂ¡c")
  void testGetCartForbiddenForAnotherUser() {
    assertThrows(
        AccessDeniedException.class,
        () -> cartController.getCart("Bearer token-user02", "user01"));
  }

  @Test
  @DisplayName("TC6: Cáº­p nháº­t sá»‘ lÆ°á»£ng theo Ä‘Ăºng user trong token")
  void testUpdateQuantity() {
    UpdateQuantityRequest request = new UpdateQuantityRequest("user01", "P001", 3);
    CartItem updatedItem = new CartItem("P001", "user01", 3, "Laptop Dell", 15000000L);

    when(cartService.updateQuantity("user01", "P001", 3)).thenReturn(updatedItem);
    when(cartService.getCartByUser("user01")).thenReturn(List.of(updatedItem));

    ResponseEntity<?> response = cartController.updateQuantity("Bearer token123", request);

    assertEquals(200, response.getStatusCode().value());
    verify(cartService).updateQuantity("user01", "P001", 3);
  }
}
