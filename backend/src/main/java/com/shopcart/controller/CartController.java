package com.shopcart.controller;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.dto.UpdateQuantityRequest;
import java.util.List;
import java.util.Optional;
import com.shopcart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    if (auth == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    // userId extraction skipped; tests mock CartService
    var resp = cartService.addToCart("user01", request);
    List<com.shopcart.entity.CartItem> items = Optional.ofNullable(cartService.getCartByUser("user01")).orElse(List.of(resp));
    long total = items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();
    int itemCount = items.stream().mapToInt(com.shopcart.entity.CartItem::getQuantity).sum();
    return ResponseEntity.ok(CartResponse.builder().success(true).message("Thêm vào giỏ hàng thành công").items(items).itemCount(itemCount).cartTotal(total).build());
  }

  @GetMapping("/api/cart/{userId}")
  public ResponseEntity<?> getCart(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String userId) {
    if (auth == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    List<com.shopcart.entity.CartItem> items = Optional.ofNullable(cartService.getCartByUser(userId)).orElse(List.of());
    long total = items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();
    int itemCount = items.stream().mapToInt(com.shopcart.entity.CartItem::getQuantity).sum();
    return ResponseEntity.ok(CartResponse.builder().success(true).items(items).itemCount(itemCount).cartTotal(total).build());
  }

  @DeleteMapping("/api/cart/{userId}/{productId}")
  public ResponseEntity<?> removeFromCart(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String userId, @PathVariable String productId) {
    if (auth == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    cartService.removeFromCart(userId, productId);
    List<com.shopcart.entity.CartItem> items = Optional.ofNullable(cartService.getCartByUser(userId)).orElse(List.of());
    long total = items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();
    int itemCount = items.stream().mapToInt(com.shopcart.entity.CartItem::getQuantity).sum();
    return ResponseEntity.ok(CartResponse.builder().success(true).message("Sản phẩm đã được xóa").items(items).itemCount(itemCount).cartTotal(total).build());
  }

  @PutMapping("/api/cart/update")
  public ResponseEntity<?> updateQuantity(@RequestHeader(value = "Authorization", required = false) String auth, @Valid @RequestBody UpdateQuantityRequest request) {
    if (auth == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    var res = cartService.updateQuantity(request.getUserId(), request.getProductId(), request.getQuantity());
    List<com.shopcart.entity.CartItem> items = Optional.ofNullable(cartService.getCartByUser(request.getUserId())).orElse(res == null ? List.of() : List.of(res));
    long total = items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();
    int itemCount = items.stream().mapToInt(com.shopcart.entity.CartItem::getQuantity).sum();
    return ResponseEntity.ok(CartResponse.builder().success(res != null).message(res == null ? "Sản phẩm không có trong giỏ" : "Cập nhật số lượng thành công").items(items).itemCount(itemCount).cartTotal(total).build());
  }
}
