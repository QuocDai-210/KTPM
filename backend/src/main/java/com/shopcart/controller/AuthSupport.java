package com.shopcart.controller;

import java.util.Map;
import java.util.Optional;

final class AuthSupport {
  private static final Map<String, String> TOKEN_TO_USER = Map.of(
      "token123", "user01",
      "token-user01", "user01",
      "token-user02", "user02");

  private AuthSupport() {}

  static Optional<String> extractUserId(String authorizationHeader) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      return Optional.empty();
    }
    if (!authorizationHeader.startsWith("Bearer ")) {
      return Optional.empty();
    }
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    if (token.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(TOKEN_TO_USER.get(token));
  }
}
