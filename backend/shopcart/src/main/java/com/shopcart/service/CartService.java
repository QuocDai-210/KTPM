package com.shopcart.service;

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
    Optional<Product> pOpt = productRepository.findById(request.getProductId());
    if (pOpt.isEmpty()) {
      throw new ProductNotFoundException("Sản phẩm không tồn tại");
    }

    Product p = pOpt.get();
    if (request.getQuantity() > p.getStock()) {
      throw new InsufficientStockException("Tồn kho không đủ");
    }

    Optional<CartItem> existing = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
    CartItem item;
    if (existing.isPresent()) {
      item = existing.get();
      item.setQuantity(item.getQuantity() + request.getQuantity());
    } else {
      item = new CartItem(p.getId(), request.getQuantity(), p.getName(), p.getPrice());
    }

    return cartRepository.save(item);
  }

  public void removeFromCart(String userId, String productId) {
    cartRepository.deleteByUserIdAndProductId(userId, productId);
  }

  public CartItem updateQuantity(String userId, String productId, int quantity) {
    Optional<CartItem> opt = cartRepository.findByUserIdAndProductId(userId, productId);
    if (opt.isEmpty()) {
      return null;
    }
    CartItem item = opt.get();
    item.setQuantity(quantity);
    return cartRepository.save(item);
  }

  public List<CartItem> getCartByUser(String userId) {
    return cartRepository.findByUserId(userId);
  }
}
