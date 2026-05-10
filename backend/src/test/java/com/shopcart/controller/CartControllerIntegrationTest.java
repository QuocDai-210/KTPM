package com.shopcart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@DisplayName("Cart API Integration Tests")
class CartControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private CartService cartService;

  @Test
  @DisplayName("POST /api/cart/add - Them san pham")
  void testAddToCart() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(2).build();
    CartItem mockItem = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);

    when(cartService.addToCart(anyString(), any(CartItemRequest.class))).thenReturn(mockItem);
    when(cartService.getCartByUser(anyString())).thenReturn(List.of(mockItem));

    mockMvc
        .perform(
            post("/api/cart/add")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Added item to cart successfully"))
        .andExpect(jsonPath("$.cartTotal").value(30000000));

    verify(cartService, times(1)).addToCart(anyString(), any(CartItemRequest.class));
  }

  @Test
  @DisplayName("GET /api/cart/{userId} - Tra ve dung response structure")
  void testGetCartResponseStructure() throws Exception {
    CartItem laptop = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);
    CartItem mouse = new CartItem("P002", "user01", 1, "Mouse Logitech", 500000L);

    when(cartService.getCartByUser("user01")).thenReturn(List.of(laptop, mouse));

    mockMvc
        .perform(get("/api/cart/{userId}", "user01").header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.cartTotal").value(30500000))
        .andExpect(jsonPath("$.itemCount").value(3));

    verify(cartService, times(1)).getCartByUser("user01");
  }

  @Test
  @DisplayName("PUT /api/cart/update - Cap nhat so luong")
  void testUpdateQuantity() throws Exception {
    UpdateQuantityRequest request = new UpdateQuantityRequest("user01", "P001", 3);
    CartItem updatedItem = new CartItem("P001", "user01", 3, "Laptop Dell", 15000000L);

    when(cartService.updateQuantity("user01", "P001", 3)).thenReturn(updatedItem);
    when(cartService.getCartByUser("user01")).thenReturn(List.of(updatedItem));

    mockMvc
        .perform(
            put("/api/cart/update")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.cartTotal").value(45000000));

    verify(cartService, times(1)).updateQuantity("user01", "P001", 3);
  }

  @Test
  @DisplayName("POST /api/cart/add - Thieu hoac sai Authorization")
  void testAddToCartUnauthorized() throws Exception {
    CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(1).build();

    mockMvc
        .perform(
            post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/api/cart/add")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());

    verify(cartService, never()).addToCart(anyString(), any(CartItemRequest.class));
  }
}
