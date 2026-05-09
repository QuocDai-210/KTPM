package com.shopcart.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcart.entity.CartItem;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Cart Repository Tests")
class CartRepositoryImplTest {
  private CartRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    repository = new CartRepositoryImpl();
    repository.clear();
  }

  @Test
  @DisplayName("save and findByUserIdAndProductId store cart item")
  void testSaveAndFindByUserIdAndProductId() {
    CartItem item = new CartItem("P001", "user01", 2, "Laptop", 15000000L);

    repository.save(item);

    var result = repository.findByUserIdAndProductId("user01", "P001");
    assertTrue(result.isPresent());
    assertEquals(2, result.get().getQuantity());
  }

  @Test
  @DisplayName("findByUserId returns only matching user items")
  void testFindByUserId() {
    repository.save(new CartItem("P001", "user01", 1, "Laptop", 15000000L));
    repository.save(new CartItem("P002", "user01", 3, "Mouse", 500000L));
    repository.save(new CartItem("P003", "user02", 2, "Keyboard", 2000000L));

    List<CartItem> userItems = repository.findByUserId("user01");

    assertEquals(2, userItems.size());
  }

  @Test
  @DisplayName("deleteByUserIdAndProductId removes the item")
  void testDeleteByUserIdAndProductId() {
    repository.save(new CartItem("P001", "user01", 1, "Laptop", 15000000L));

    repository.deleteByUserIdAndProductId("user01", "P001");

    assertFalse(repository.findByUserIdAndProductId("user01", "P001").isPresent());
  }

  @Test
  @DisplayName("save rejects missing userId")
  void testSaveRejectsMissingUserId() {
    CartItem item = new CartItem("P001", null, 1, "Laptop", 15000000L);

    assertThrows(IllegalArgumentException.class, () -> repository.save(item));
  }
}
