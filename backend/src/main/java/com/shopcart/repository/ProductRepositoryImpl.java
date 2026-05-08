package com.shopcart.repository;

import com.shopcart.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ProductRepositoryImpl implements ProductRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void seedProducts() {
    Long count = entityManager
        .createQuery("select count(p) from Product p", Long.class)
        .getSingleResult();
    if (count > 0) {
      return;
    }

    List.of(
        new Product("P001", "Laptop Dell XPS 13", 15000000L, 10),
        new Product("P002", "Mouse Logitech MX Master", 500000L, 50),
        new Product("P003", "Keyboard Mechanical Keychron K2", 2000000L, 20),
        new Product("P004", "Monitor LG UltraGear 27", 6500000L, 15),
        new Product("P005", "Headset Sony WH-1000XM5", 8500000L, 12),
        new Product("P006", "Webcam Logitech C920", 1800000L, 25),
        new Product("P007", "SSD Samsung 1TB", 2500000L, 30),
        new Product("P008", "Router TP-Link AX3000", 2200000L, 18),
        new Product("P009", "iPad Air", 16000000L, 8),
        new Product("P010", "USB-C Hub Anker 7-in-1", 1200000L, 40))
        .forEach(entityManager::persist);
  }

  @Override
  public Optional<Product> findById(String id) {
    return Optional.ofNullable(entityManager.find(Product.class, id));
  }

  @Override
  public List<Product> findAll() {
    return entityManager
        .createQuery("select p from Product p order by p.id", Product.class)
        .getResultList();
  }

  @Override
  @Transactional
  public Product save(Product product) {
    return entityManager.merge(product);
  }
}
