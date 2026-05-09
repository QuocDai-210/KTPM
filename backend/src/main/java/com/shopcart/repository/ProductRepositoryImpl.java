package com.shopcart.repository;

import com.shopcart.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ProductRepositoryImpl implements ProductRepository {
  @PersistenceContext
  private EntityManager entityManager;

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
