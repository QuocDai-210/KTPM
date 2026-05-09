package com.shopcart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.shopcart.common.ApiMessages;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.exception.ProductNotFoundException;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.ProductRepository;

@Service
public class CartService {
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;

  public CartService(ProductRepository productRepository, CartRepository cartRepository) {
    this.productRepository = productRepository;
    this.cartRepository = cartRepository;
  }

  public CartItem addToCart(String userId, CartItemRequest request) {
    validateAddToCartRequest(request);

    Product product = getActiveProduct(request.getProductId());
    Optional<CartItem> existing = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
    int currentQuantity = existing.map(CartItem::getQuantity).orElse(0);
    int nextQuantity = currentQuantity + request.getQuantity();
    validateStock(product, nextQuantity);

    CartItem item = existing.orElseGet(() -> buildCartItem(product));
    item.setQuantity(nextQuantity);
    item.setUserId(userId);

    return cartRepository.save(item);
  }

  public void removeFromCart(String userId, String productId) {
    cartRepository.deleteByUserIdAndProductId(userId, productId);
  }

  public CartItem updateQuantity(String userId, String productId, int quantity) {
    validateQuantity(quantity);

    Optional<CartItem> itemOptional = cartRepository.findByUserIdAndProductId(userId, productId);
    if (itemOptional.isEmpty()) {
      return null;
    }

    Product product = getActiveProduct(productId);
    validateStock(product, quantity);

    CartItem item = itemOptional.get();
    item.setQuantity(quantity);
    return cartRepository.save(item);
  }

  public List<CartItem> getCartByUser(String userId) {
    return cartRepository.findByUserId(userId);
  }

  private void validateAddToCartRequest(CartItemRequest request) {
    if (request == null) {
      throw new IllegalArgumentException(ApiMessages.PRODUCT_ID_REQUIRED);
    }

    if (request.getProductId() == null || request.getProductId().isBlank()) {
      throw new IllegalArgumentException(ApiMessages.PRODUCT_ID_REQUIRED);
    }

    validateQuantity(request.getQuantity());
  }

  private void validateQuantity(Integer quantity) {
    if (quantity == null || quantity < 1) {
      throw new IllegalArgumentException(ApiMessages.QUANTITY_MUST_BE_POSITIVE);
    }
  }

  private Product getActiveProduct(String productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(ApiMessages.PRODUCT_NOT_FOUND));

    if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
      throw new IllegalArgumentException(ApiMessages.PRODUCT_INACTIVE);
    }

    return product;
  }

  private void validateStock(Product product, int quantity) {
    if (quantity > product.getStock()) {
      throw new InsufficientStockException(ApiMessages.INSUFFICIENT_STOCK);
    }
  }

  private CartItem buildCartItem(Product product) {
    return new CartItem(product.getId(), 0, product.getName(), product.getPrice());
  }
}
