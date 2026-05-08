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
    if (request.getProductId() == null || request.getProductId().isBlank()) {
      throw new IllegalArgumentException("Product ID không được rỗng");
    }
    if (request.getQuantity() == null || request.getQuantity() < 1) {
      throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
    }

    Optional<Product> pOpt = productRepository.findById(request.getProductId());
    if (pOpt.isEmpty()) {
      throw new ProductNotFoundException("Sản phẩm không tồn tại");
    }

    Product p = pOpt.get();
    if (!"ACTIVE".equalsIgnoreCase(p.getStatus())) {
      throw new IllegalArgumentException("Sản phẩm không còn được bán");
    }
    Optional<CartItem> existing = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
    int currentQuantity = existing.map(CartItem::getQuantity).orElse(0);
    int nextQuantity = currentQuantity + request.getQuantity();
    if (nextQuantity > p.getStock()) {
      throw new InsufficientStockException("Tồn kho không đủ");
    }

    CartItem item;
    if (existing.isPresent()) {
      item = existing.get();
      item.setQuantity(nextQuantity);
    } else {
      item = new CartItem(p.getId(), request.getQuantity(), p.getName(), p.getPrice());
    }

    item.setUserId(userId);
    return cartRepository.save(item);
  }

  public void removeFromCart(String userId, String productId) {
    cartRepository.deleteByUserIdAndProductId(userId, productId);
  }

  public CartItem updateQuantity(String userId, String productId, int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
    }
    Optional<CartItem> opt = cartRepository.findByUserIdAndProductId(userId, productId);
    if (opt.isEmpty()) {
      return null;
    }
    Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Sản phẩm không tồn tại"));
    if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
      throw new IllegalArgumentException("Sản phẩm không còn được bán");
    }
    if (quantity > product.getStock()) {
      throw new InsufficientStockException("Tồn kho không đủ");
    }
    CartItem item = opt.get();
    item.setQuantity(quantity);
    return cartRepository.save(item);
  }

  public List<CartItem> getCartByUser(String userId) {
    return cartRepository.findByUserId(userId);
  }
}
