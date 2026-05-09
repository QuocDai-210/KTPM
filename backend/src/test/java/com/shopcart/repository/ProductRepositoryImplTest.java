package com.shopcart.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcart.ShopcartBackendApplication;
import com.shopcart.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ShopcartBackendApplication.class)
@DisplayName("Product Repository Integration Tests")
class ProductRepositoryImplTest {
  @Autowired private ProductRepository productRepository;

  @Test
  @DisplayName("findAll returns seeded products")
  void testFindAllReturnsSeededProducts() {
    var products = productRepository.findAll();

    assertTrue(products.size() >= 10);
  }

  @Test
  @DisplayName("findById returns seeded product")
  void testFindByIdReturnsProduct() {
    var product = productRepository.findById("P001");

    assertTrue(product.isPresent());
    assertEquals("P001", product.get().getId());
  }

  @Test
  @DisplayName("save persists product changes")
  void testSavePersistsChanges() {
    Product product = productRepository.findById("P002").orElseThrow();
    product.setStock(77);

    productRepository.save(product);

    assertEquals(77, productRepository.findById("P002").orElseThrow().getStock());
  }
}
