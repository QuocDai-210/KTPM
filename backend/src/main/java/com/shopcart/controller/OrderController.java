package com.shopcart.controller;

import com.shopcart.common.ApiMessages;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/api/orders")
  public ResponseEntity<?> createOrder(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @Valid @RequestBody OrderRequest request) {
    String authenticatedUserId = requireUser(auth);
    if (request.getUserId() == null || request.getUserId().isBlank()) {
      request.setUserId(authenticatedUserId);
    }
    verifyOwnership(authenticatedUserId, request.getUserId());
    OrderResponse response = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/api/order/{orderId}")
  public ResponseEntity<?> getOrder(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable String orderId) {
    String authenticatedUserId = requireUser(auth);
    var order = orderService.getOrderForUser(orderId, authenticatedUserId);
    return ResponseEntity.ok(order);
  }

  @PatchMapping("/api/order/{orderId}/cancel")
  public ResponseEntity<?> cancelOrder(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable String orderId) {
    String authenticatedUserId = requireUser(auth);
    orderService.cancelOrderForUser(orderId, authenticatedUserId);
    return ResponseEntity.ok().build();
  }

  private String requireUser(String auth) {
    return AuthSupport.extractUserId(auth)
        .orElseThrow(() -> new AccessDeniedException(ApiMessages.INVALID_AUTHORIZATION));
  }

  private void verifyOwnership(String authenticatedUserId, String requestedUserId) {
    if (!authenticatedUserId.equals(requestedUserId)) {
      throw new AccessDeniedException(ApiMessages.ORDER_OPERATION_FORBIDDEN);
    }
  }
}
