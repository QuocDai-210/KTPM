package com.shopcart.service;

import com.shopcart.common.ApiMessages;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.exception.ProductNotFoundException;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CartService {
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;

  public CartService(ProductRepository productRepository, CartRepository cartRepository) {
    this.productRepository = productRepository;
    this.cartRepository = cartRepository;
  }

  public CartItem addToCart(String userId, CartItemRequest request) {
    if (request.getProductId() == null || request.getProductId().isBlank()) {
      throw new IllegalArgumentException(ApiMessages.PRODUCT_ID_REQUIRED);
    }
    if (request.getQuantity() == null || request.getQuantity() < 1) {
      throw new IllegalArgumentException(ApiMessages.QUANTITY_MUST_BE_POSITIVE);
    }

    Optional<Product> productOptional = productRepository.findById(request.getProductId());
    if (productOptional.isEmpty()) {
      throw new ProductNotFoundException(ApiMessages.PRODUCT_NOT_FOUND);
    }

    Product product = productOptional.get();
    if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
      throw new IllegalArgumentException(ApiMessages.PRODUCT_INACTIVE);
    }

    Optional<CartItem> existing = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
    int currentQuantity = existing.map(CartItem::getQuantity).orElse(0);
    int nextQuantity = currentQuantity + request.getQuantity();
    if (nextQuantity > product.getStock()) {
      throw new InsufficientStockException(ApiMessages.INSUFFICIENT_STOCK);
    }

    CartItem item;
    if (existing.isPresent()) {
      item = existing.get();
      item.setQuantity(nextQuantity);
    } else {
      item = new CartItem(product.getId(), request.getQuantity(), product.getName(), product.getPrice());
    }

    item.setUserId(userId);
    return cartRepository.save(item);
  }

  public void removeFromCart(String userId, String productId) {
    cartRepository.deleteByUserIdAndProductId(userId, productId);
  }

  public CartItem updateQuantity(String userId, String productId, int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException(ApiMessages.QUANTITY_MUST_BE_POSITIVE);
    }

    Optional<CartItem> itemOptional = cartRepository.findByUserIdAndProductId(userId, productId);
    if (itemOptional.isEmpty()) {
      return null;
    }

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(ApiMessages.PRODUCT_NOT_FOUND));
    if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
      throw new IllegalArgumentException(ApiMessages.PRODUCT_INACTIVE);
    }
    if (quantity > product.getStock()) {
      throw new InsufficientStockException(ApiMessages.INSUFFICIENT_STOCK);
    }

    CartItem item = itemOptional.get();
    item.setQuantity(quantity);
    return cartRepository.save(item);
  }

  public List<CartItem> getCartByUser(String userId) {
    return cartRepository.findByUserId(userId);
  }
}
