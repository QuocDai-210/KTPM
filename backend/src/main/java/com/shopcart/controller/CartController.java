package com.shopcart.controller;

import com.shopcart.common.ApiMessages;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.dto.UpdateQuantityRequest;
import com.shopcart.service.CartService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PostMapping("/api/cart/add")
  public ResponseEntity<?> addToCart(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @Valid @RequestBody CartItemRequest request) {
    Optional<String> userId = AuthSupport.extractUserId(auth);
    if (userId.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    var responseItem = cartService.addToCart(userId.get(), request);
    List<com.shopcart.entity.CartItem> items =
        Optional.ofNullable(cartService.getCartByUser(userId.get())).orElse(List.of(responseItem));
    return ResponseEntity.ok(buildCartResponse(items, true, ApiMessages.CART_ADD_SUCCESS));
  }

  @GetMapping("/api/cart/{userId}")
  public ResponseEntity<?> getCart(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable String userId) {
    String authenticatedUserId = requireUser(auth);
    verifyOwnership(authenticatedUserId, userId);
    List<com.shopcart.entity.CartItem> items =
        Optional.ofNullable(cartService.getCartByUser(userId)).orElse(List.of());
    return ResponseEntity.ok(buildCartResponse(items, true, null));
  }

  @DeleteMapping("/api/cart/{userId}/{productId}")
  public ResponseEntity<?> removeFromCart(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable String userId,
      @PathVariable String productId) {
    String authenticatedUserId = requireUser(auth);
    verifyOwnership(authenticatedUserId, userId);
    cartService.removeFromCart(userId, productId);
    List<com.shopcart.entity.CartItem> items =
        Optional.ofNullable(cartService.getCartByUser(userId)).orElse(List.of());
    return ResponseEntity.ok(buildCartResponse(items, true, ApiMessages.CART_REMOVE_SUCCESS));
  }

  @PutMapping("/api/cart/update")
  public ResponseEntity<?> updateQuantity(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @Valid @RequestBody UpdateQuantityRequest request) {
    String authenticatedUserId = requireUser(auth);
    verifyOwnership(authenticatedUserId, request.getUserId());
    var result = cartService.updateQuantity(request.getUserId(), request.getProductId(), request.getQuantity());
    List<com.shopcart.entity.CartItem> items =
        Optional.ofNullable(cartService.getCartByUser(request.getUserId()))
            .orElse(result == null ? List.of() : List.of(result));
    String message = result == null ? ApiMessages.CART_ITEM_NOT_FOUND : ApiMessages.CART_UPDATE_SUCCESS;
    return ResponseEntity.ok(buildCartResponse(items, result != null, message));
  }

  private String requireUser(String auth) {
    return AuthSupport.extractUserId(auth)
        .orElseThrow(() -> new AccessDeniedException(ApiMessages.INVALID_AUTHORIZATION));
  }

  private void verifyOwnership(String authenticatedUserId, String requestedUserId) {
    if (!authenticatedUserId.equals(requestedUserId)) {
      throw new AccessDeniedException(ApiMessages.CART_ACCESS_FORBIDDEN);
    }
  }

  private CartResponse buildCartResponse(
      List<com.shopcart.entity.CartItem> items, boolean success, String message) {
    long total = items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();
    int itemCount = items.stream().mapToInt(com.shopcart.entity.CartItem::getQuantity).sum();
    return CartResponse.builder()
        .success(success)
        .message(message)
        .items(items)
        .itemCount(itemCount)
        .cartTotal(total)
        .build();
  }
}
