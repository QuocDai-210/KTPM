package com.shopcart.controller;

import com.shopcart.dto.CartResponse;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<CartResponse> handleInsufficient(InsufficientStockException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CartResponse.builder().success(false).message(ex.getMessage()).build());
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<CartResponse> handleNotFound(ProductNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CartResponse.builder().success(false).message(ex.getMessage()).build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<CartResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CartResponse.builder().success(false).message(ex.getMessage()).build());
  }
}
