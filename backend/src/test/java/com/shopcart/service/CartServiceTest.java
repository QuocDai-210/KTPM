package com.shopcart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.exception.ProductNotFoundException;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.ProductRepository;

@DisplayName("Cart Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private CartRepository cartRepository;

  @InjectMocks private CartService cartService;

  @BeforeEach
  void setUp() {
    // Setup initial state
  }

  @Test
  @DisplayName("TC1: Thêm sản phẩm vào giỏ hàng thành công")
  void testAddToCartSuccess() {
    // Arrange
    Product product = new Product("P001", "Laptop Dell", 15000000L, 10);
    CartItemRequest request = new CartItemRequest("P001", 2);

    when(productRepository.findById("P001")).thenReturn(Optional.of(product));
    when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    CartItem result = cartService.addToCart("user01", request);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getQuantity());
    assertEquals("Laptop Dell", result.getProductName());
    assertEquals(15000000L, result.getPrice());
    verify(cartRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  @DisplayName("TC2: Thêm sản phẩm đã có trong giỏ (cộng dồn số lượng)")
  void testAddToCartExistingProduct() {
    // Arrange
    Product product = new Product("P001", "Laptop Dell", 15000000L, 10);
    CartItemRequest request = new CartItemRequest("P001", 2);

    CartItem existingItem = new CartItem("P001", 1, "Laptop Dell", 15000000L);

    when(productRepository.findById("P001")).thenReturn(Optional.of(product));
    when(cartRepository.findByUserIdAndProductId("user01", "P001"))
        .thenReturn(Optional.of(existingItem));
    when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    CartItem result = cartService.addToCart("user01", request);

    // Assert
    assertNotNull(result);
    assertEquals(3, result.getQuantity()); // 1 + 2
    verify(cartRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  @DisplayName("TC3: Thêm sản phẩm khi tồn kho không đủ")
  void testAddToCartInsufficientStock() {
    // Arrange
    Product product = new Product("P001", "Laptop Dell", 15000000L, 5);
    CartItemRequest request = new CartItemRequest("P001", 10); // Request > stock

    when(productRepository.findById("P001")).thenReturn(Optional.of(product));

    // Act & Assert
    assertThrows(
        InsufficientStockException.class,
        () -> cartService.addToCart("user01", request));
    verify(cartRepository, never()).save(any(CartItem.class));
  }

  @Test
  @DisplayName("TC4: Thêm sản phẩm không tồn tại")
  void testAddToCartProductNotFound() {
    // Arrange
    CartItemRequest request = new CartItemRequest("INVALID", 2);

    when(productRepository.findById("INVALID")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ProductNotFoundException.class,
        () -> cartService.addToCart("user01", request));
    verify(cartRepository, never()).save(any(CartItem.class));
  }

  @Test
  @DisplayName("TC5: Xóa sản phẩm khỏi giỏ hàng")
  void testRemoveFromCart() {
    // Arrange
    String userId = "user01";
    String productId = "P001";

    // Act
    cartService.removeFromCart(userId, productId);

    // Assert
    verify(cartRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
  }

  @Test
  @DisplayName("TC6: Cập nhật số lượng sản phẩm trong giỏ")
  void testUpdateQuantity() {
    // Arrange
    CartItem cartItem = new CartItem("P001", 2, "Laptop Dell", 15000000L);
    int newQuantity = 5;

    when(cartRepository.findByUserIdAndProductId("user01", "P001"))
        .thenReturn(Optional.of(cartItem));
    when(productRepository.findById("P001"))
        .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
    when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    CartItem result = cartService.updateQuantity("user01", "P001", newQuantity);

    // Assert
    assertNotNull(result);
    assertEquals(5, result.getQuantity());
    verify(cartRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  @DisplayName("TC7: Coverage test - Get cart by user")
  void testGetCartByUser() {
    // Arrange
    String userId = "user01";

    // Act
    cartService.getCartByUser(userId);

    // Assert
    verify(cartRepository, times(1)).findByUserId(userId);
  }

  @Test
  @DisplayName("TC8: Cập nhật số lượng vượt quá tồn kho")
  void testUpdateQuantityExceedStock() {
    // Arrange
    CartItem cartItem = new CartItem("P001", 2, "Laptop Dell", 15000000L);

    when(cartRepository.findByUserIdAndProductId("user01", "P001"))
        .thenReturn(Optional.of(cartItem));
    when(productRepository.findById("P001"))
        .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 5)));

    // Act & Assert
    assertThrows(
        InsufficientStockException.class,
        () -> cartService.updateQuantity("user01", "P001", 10));
    verify(cartRepository, never()).save(any(CartItem.class));
  }

  @Test
  @DisplayName("TC9: Xóa sản phẩm không tồn tại trong giỏ")
  void testRemoveNonExistentProduct() {
    // Arrange
    String userId = "user01";
    String productId = "INVALID";

    when(cartRepository.findByUserIdAndProductId(userId, productId))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        Exception.class,
        () -> cartService.removeFromCart(userId, productId));
  }

  @Test
  @DisplayName("TC10: Quantity validation - Zero quantity")
  void testAddToCartZeroQuantity() {
    // Arrange
    CartItemRequest request = new CartItemRequest("P001", 0);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> cartService.addToCart("user01", request));
  }

  @Test
  @DisplayName("TC11: Quantity validation - Negative quantity")
  void testAddToCartNegativeQuantity() {
    // Arrange
    CartItemRequest request = new CartItemRequest("P001", -5);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> cartService.addToCart("user01", request));
  }

  @Test
  @DisplayName("TC12: Clear entire cart")
  void testClearCart() {
    // Arrange
    String userId = "user01";

    // Act
    cartService.clearCart(userId);

    // Assert
    verify(cartRepository, times(1)).deleteByUserId(userId);
  }

  @Test
  @DisplayName("TC13: Calculate cart total price")
  void testCalculateCartTotal() {
    // Arrange
    CartItem item1 = new CartItem("P001", 2, "Laptop", 15000000L);
    CartItem item2 = new CartItem("P002", 1, "Mouse", 500000L);
    java.util.List<CartItem> items = java.util.List.of(item1, item2);

    // Act
    Long total = cartService.calculateCartTotal(items);

    // Assert
    assertEquals(30500000L, total); // (15M * 2) + (500k * 1)
  }

  @Test
  @DisplayName("TC14: Get cart count")
  void testGetCartCount() {
    // Arrange
    CartItem item1 = new CartItem("P001", 2, "Laptop", 15000000L);
    CartItem item2 = new CartItem("P002", 1, "Mouse", 500000L);
    java.util.List<CartItem> items = java.util.List.of(item1, item2);

    // Act
    int count = cartService.getCartItemCount(items);

    // Assert
    assertEquals(3, count); // 2 + 1
  }
}
