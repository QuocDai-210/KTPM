package com.shopcart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.dto.UpdateQuantityRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.service.CartService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@DisplayName("Cart API Integration Tests")
@ExtendWith(MockitoExtension.class)
class CartControllerIntegrationTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CartService cartService;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CartController(cartService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(corsFilter())
            .build();
  }

  @Nested
  @DisplayName("a) Test POST /api/cart/add endpoint")
  class AddToCartEndpointTests {

    @Test
    @DisplayName("POST /api/cart/add - Them san pham vao gio hang")
    void testAddToCartEndpoint() throws Exception {
      CartItemRequest request = CartItemRequest.builder().productId("P001").quantity(2).build();
      CartItem mockCartItem = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);
      CartResponse mockResponse =
          CartResponse.builder()
              .success(true)
              .message("Added item to cart successfully")
              .cartTotal(30000000L)
              .items(List.of(mockCartItem))
              .itemCount(2)
              .build();

      when(cartService.addToCart(anyString(), any(CartItemRequest.class))).thenReturn(mockCartItem);
      when(cartService.getCartByUser("user01")).thenReturn(List.of(mockCartItem));

      mockMvc
          .perform(
              post("/api/cart/add")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json"))
          .andExpect(jsonPath("$.success").value(mockResponse.isSuccess()))
          .andExpect(jsonPath("$.message").value(mockResponse.getMessage()))
          .andExpect(jsonPath("$.cartTotal").value(mockResponse.getCartTotal()))
          .andExpect(jsonPath("$.itemCount").value(mockResponse.getItemCount()))
          .andExpect(jsonPath("$.items[0].productId").value("P001"))
          .andExpect(jsonPath("$.items[0].productName").value("Laptop Dell"))
          .andExpect(jsonPath("$.items[0].quantity").value(2));

      verify(cartService, times(1)).addToCart(anyString(), any(CartItemRequest.class));
      verify(cartService, times(1)).getCartByUser("user01");
    }
  }

  @Nested
  @DisplayName("b) Test response structure va status codes")
  class ResponseStructureAndStatusCodeTests {

    @Test
    @DisplayName("GET /api/cart/{userId} - Tra ve dung response structure")
    void testGetCartResponseStructure() throws Exception {
      CartItem laptop = new CartItem("P001", "user01", 2, "Laptop Dell", 15000000L);
      CartItem mouse = new CartItem("P002", "user01", 1, "Mouse Logitech", 500000L);
      CartResponse mockResponse =
          CartResponse.builder()
              .success(true)
              .cartTotal(30500000L)
              .items(List.of(laptop, mouse))
              .itemCount(3)
              .build();

      when(cartService.getCartByUser("user01")).thenReturn(List.of(laptop, mouse));

      mockMvc
          .perform(get("/api/cart/{userId}", "user01").header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.success").value(mockResponse.isSuccess()))
          .andExpect(jsonPath("$.cartTotal").value(mockResponse.getCartTotal()))
          .andExpect(jsonPath("$.itemCount").value(mockResponse.getItemCount()))
          .andExpect(jsonPath("$.items.length()").value(mockResponse.getItems().size()));

      verify(cartService, times(1)).getCartByUser("user01");
    }

    @Test
    @DisplayName("PUT /api/cart/update - Cap nhat so luong thanh cong")
    void testUpdateQuantityEndpoint() throws Exception {
      UpdateQuantityRequest request = new UpdateQuantityRequest("user01", "P001", 3);
      CartItem updatedItem = new CartItem("P001", "user01", 3, "Laptop Dell", 15000000L);
      CartResponse mockResponse =
          CartResponse.builder()
              .success(true)
              .message("Updated cart quantity successfully")
              .cartTotal(45000000L)
              .items(List.of(updatedItem))
              .itemCount(3)
              .build();

      when(cartService.updateQuantity("user01", "P001", 3)).thenReturn(updatedItem);
      when(cartService.getCartByUser("user01")).thenReturn(List.of(updatedItem));

      mockMvc
          .perform(
              put("/api/cart/update")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(mockResponse.isSuccess()))
          .andExpect(jsonPath("$.message").value(mockResponse.getMessage()))
          .andExpect(jsonPath("$.cartTotal").value(mockResponse.getCartTotal()))
          .andExpect(jsonPath("$.itemCount").value(mockResponse.getItemCount()));

      verify(cartService, times(1)).updateQuantity("user01", "P001", 3);
    }

    @Test
    @DisplayName("DELETE /api/cart/{userId}/{productId} - Xoa san pham khoi gio hang")
    void testRemoveFromCartEndpoint() throws Exception {
      CartResponse mockResponse =
          CartResponse.builder()
              .success(true)
              .message("Removed item from cart successfully")
              .cartTotal(0L)
              .items(List.of())
              .itemCount(0)
              .build();

      when(cartService.getCartByUser("user01")).thenReturn(List.of());

      mockMvc
          .perform(
              delete("/api/cart/{userId}/{productId}", "user01", "P001")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer token123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(mockResponse.isSuccess()))
          .andExpect(jsonPath("$.message").value(mockResponse.getMessage()))
          .andExpect(jsonPath("$.cartTotal").value(mockResponse.getCartTotal()))
          .andExpect(jsonPath("$.itemCount").value(mockResponse.getItemCount()));

      verify(cartService, times(1)).removeFromCart("user01", "P001");
      verify(cartService, times(1)).getCartByUser("user01");
    }

    @Test
    @DisplayName("POST /api/cart/add - Thieu hoac sai Authorization tra ve 401")
    void testAddToCartUnauthorizedStatusCodes() throws Exception {
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

    @Test
    @DisplayName("GET /api/cart/{userId} - Khong cho truy cap gio hang user khac")
    void testGetCartForbiddenForAnotherUser() throws Exception {
      CartResponse mockResponse =
          CartResponse.builder()
              .success(false)
              .message("Cannot access another user's cart")
              .build();

      mockMvc
          .perform(get("/api/cart/{userId}", "user01").header(HttpHeaders.AUTHORIZATION, "Bearer token-user02"))
          .andExpect(status().isForbidden())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.success").value(mockResponse.isSuccess()))
          .andExpect(jsonPath("$.message").value(mockResponse.getMessage()));

      verify(cartService, never()).getCartByUser("user01");
    }
  }

  @Nested
  @DisplayName("c) Test CORS va headers")
  class CorsAndHeaderTests {

    @Test
    @DisplayName("OPTIONS /api/cart/add - Tra ve CORS headers cho frontend")
    void testAddToCartCorsPreflightHeaders() throws Exception {
      mockMvc
          .perform(
              options("/api/cart/add")
                  .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                  .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                  .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
          .andExpect(status().isOk())
          .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
          .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type"))
          .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE"));

      verify(cartService, never()).addToCart(anyString(), any(CartItemRequest.class));
    }
  }

  private CorsFilter corsFilter() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000");
    configuration.addAllowedOrigin("http://localhost:5173");
    configuration.addAllowedMethod("GET");
    configuration.addAllowedMethod("POST");
    configuration.addAllowedMethod("PUT");
    configuration.addAllowedMethod("PATCH");
    configuration.addAllowedMethod("DELETE");
    configuration.addAllowedHeader("Authorization");
    configuration.addAllowedHeader("Content-Type");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return new CorsFilter(source);
  }
}
