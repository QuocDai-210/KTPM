package com.shopcart.database.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.entity.Inventory;
import com.shopcart.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductSeedLoader {
  private static final String PRODUCTS_SEED_PATH = "database/seeds/products.json";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @PersistenceContext
  private EntityManager entityManager;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void seedProducts() {
    loadProducts()
        .forEach(
            product -> {
              if (entityManager.find(Product.class, product.getId()) == null) {
                entityManager.persist(product);
              }
              if (entityManager.find(Inventory.class, product.getId()) == null) {
                entityManager.persist(new Inventory(product.getId(), product.getStock()));
              }
            });
  }

  private List<Product> loadProducts() {
    try (InputStream inputStream = new ClassPathResource(PRODUCTS_SEED_PATH).getInputStream()) {
      return objectMapper.readValue(inputStream, new TypeReference<>() {});
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot load product seed data from " + PRODUCTS_SEED_PATH, exception);
    }
  }
}
