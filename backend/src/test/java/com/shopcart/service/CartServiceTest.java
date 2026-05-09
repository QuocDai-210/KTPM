package com.shopcart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

  @Nested
  @DisplayName("addToCart()")
  class AddToCart {

    @Test
    @DisplayName("TC1: Thêm sản phẩm vào giỏ hàng thành công")
    void testAddToCartSuccess() {
      Product product = new Product("P001", "Laptop Dell", 15000000L, 10);
      CartItemRequest request = new CartItemRequest("P001", 2);

      when(productRepository.findById("P001")).thenReturn(Optional.of(product));
      when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

      CartItem result = cartService.addToCart("user01", request);

      assertNotNull(result);
      assertEquals(2, result.getQuantity());
      assertEquals("Laptop Dell", result.getProductName());
      assertEquals(15000000L, result.getPrice());
      verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC2: Thêm sản phẩm đã có trong giỏ (cộng dồn số lượng)")
    void testAddToCartExistingProduct() {
      Product product = new Product("P001", "Laptop Dell", 15000000L, 10);
      CartItemRequest request = new CartItemRequest("P001", 2);
      CartItem existingItem = new CartItem("P001", 1, "Laptop Dell", 15000000L);

      when(productRepository.findById("P001")).thenReturn(Optional.of(product));
      when(cartRepository.findByUserIdAndProductId("user01", "P001"))
          .thenReturn(Optional.of(existingItem));
      when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

      CartItem result = cartService.addToCart("user01", request);

      assertNotNull(result);
      assertEquals(3, result.getQuantity());
      verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC3: Thêm sản phẩm khi tồn kho không đủ")
    void testAddToCartInsufficientStock() {
      Product product = new Product("P001", "Laptop Dell", 15000000L, 5);
      CartItemRequest request = new CartItemRequest("P001", 10);

      when(productRepository.findById("P001")).thenReturn(Optional.of(product));

      assertThrows(
          InsufficientStockException.class,
          () -> cartService.addToCart("user01", request));
      verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC4: Thêm sản phẩm không tồn tại")
    void testAddToCartProductNotFound() {
      CartItemRequest request = new CartItemRequest("INVALID", 2);

      when(productRepository.findById("INVALID")).thenReturn(Optional.empty());

      assertThrows(
          ProductNotFoundException.class,
          () -> cartService.addToCart("user01", request));
      verify(cartRepository, never()).save(any(CartItem.class));
    }
  }

  @Nested
  @DisplayName("removeFromCart()")
  class RemoveFromCart {

    @Test
    @DisplayName("TC1: Xóa sản phẩm khỏi giỏ hàng")
    void testRemoveFromCart() {
      String userId = "user01";
      String productId = "P001";
      List<CartItem> cartItems = new ArrayList<>();
      cartItems.add(new CartItem("P001", 2, "Laptop Dell", 15000000L));
      cartItems.add(new CartItem("P002", 1, "Mouse Logitech", 500000L));

      doAnswer(inv -> {
        cartItems.removeIf(item -> item.getProductId().equals(productId));
        return null;
      }).when(cartRepository).deleteByUserIdAndProductId(userId, productId);
      when(cartRepository.findByUserId(userId)).thenReturn(cartItems);

      cartService.removeFromCart(userId, productId);
      List<CartItem> result = cartService.getCartByUser(userId);

      assertEquals(1, result.size());
      assertFalse(result.stream().anyMatch(item -> item.getProductId().equals(productId)));
      verify(cartRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
    }

    @Test
    @DisplayName("TC2: Xóa sản phẩm không tồn tại trong giỏ")
    void testRemoveNonExistentProduct() {
      String userId = "user01";
      String productId = "INVALID";

      assertDoesNotThrow(() -> cartService.removeFromCart(userId, productId));

      verify(cartRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
    }
  }

  @Nested
  @DisplayName("updateQuantity()")
  class UpdateQuantity {

    @Test
    @DisplayName("TC1: Cập nhật số lượng sản phẩm trong giỏ")
    void testUpdateQuantity() {
      CartItem cartItem = new CartItem("P001", 2, "Laptop Dell", 15000000L);

      when(cartRepository.findByUserIdAndProductId("user01", "P001"))
          .thenReturn(Optional.of(cartItem));
      when(productRepository.findById("P001"))
          .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 10)));
      when(cartRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

      CartItem result = cartService.updateQuantity("user01", "P001", 5);

      assertNotNull(result);
      assertEquals(5, result.getQuantity());
      assertEquals(5, cartItem.getQuantity());
      verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC2: Cập nhật số lượng vượt quá tồn kho")
    void testUpdateQuantityExceedStock() {
      CartItem cartItem = new CartItem("P001", 2, "Laptop Dell", 15000000L);

      when(cartRepository.findByUserIdAndProductId("user01", "P001"))
          .thenReturn(Optional.of(cartItem));
      when(productRepository.findById("P001"))
          .thenReturn(Optional.of(new Product("P001", "Laptop Dell", 15000000L, 5)));

      assertThrows(
          InsufficientStockException.class,
          () -> cartService.updateQuantity("user01", "P001", 10));
      verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC3: Cập nhật số lượng khi sản phẩm không có trong giỏ")
    void testUpdateQuantityItemNotFound() {
      when(cartRepository.findByUserIdAndProductId("user01", "P404"))
          .thenReturn(Optional.empty());

      CartItem result = cartService.updateQuantity("user01", "P404", 2);

      assertNull(result);
      verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC4: Cập nhật số lượng về 0")
    void testUpdateQuantityZeroQuantity() {
      assertThrows(
          IllegalArgumentException.class,
          () -> cartService.updateQuantity("user01", "P001", 0));
      verify(cartRepository, never()).findByUserIdAndProductId("user01", "P001");
      verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC5: Cập nhật số lượng âm")
    void testUpdateQuantityNegativeQuantity() {
      assertThrows(
          IllegalArgumentException.class,
          () -> cartService.updateQuantity("user01", "P001", -1));
      verify(cartRepository, never()).findByUserIdAndProductId("user01", "P001");
      verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC6: Cập nhật số lượng khi sản phẩm không tồn tại")
    void testUpdateQuantityProductNotFound() {
      CartItem cartItem = new CartItem("P001", 2, "Laptop Dell", 15000000L);

      when(cartRepository.findByUserIdAndProductId("user01", "P001"))
          .thenReturn(Optional.of(cartItem));
      when(productRepository.findById("P001")).thenReturn(Optional.empty());

      assertThrows(
          ProductNotFoundException.class,
          () -> cartService.updateQuantity("user01", "P001", 5));
      verify(cartRepository, never()).save(any(CartItem.class));
    }
  }
}
