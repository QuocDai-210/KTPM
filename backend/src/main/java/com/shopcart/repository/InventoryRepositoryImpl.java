package com.shopcart.repository;

import com.shopcart.entity.Inventory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class InventoryRepositoryImpl implements InventoryRepository {
  @PersistenceContext private EntityManager entityManager;

  @Override
  public Optional<Inventory> findByProductId(String productId) {
    return Optional.ofNullable(entityManager.find(Inventory.class, productId));
  }

  @Override
  @Transactional
  public Inventory save(Inventory inventory) {
    return entityManager.merge(inventory);
  }
}
