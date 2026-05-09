package com.shopcart.controller;

import com.shopcart.dto.CartResponse;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.exception.ProductNotFoundException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<CartResponse> handleInsufficient(InsufficientStockException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<CartResponse> handleNotFound(ProductNotFoundException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<CartResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<CartResponse> handleAccessDenied(AccessDeniedException ex) {
    return build(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<CartResponse> handleNoSuchElement(NoSuchElementException ex) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  private ResponseEntity<CartResponse> build(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(CartResponse.builder().success(false).message(message).build());
  }
}
