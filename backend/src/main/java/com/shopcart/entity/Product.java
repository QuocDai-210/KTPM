package com.shopcart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Product {
  @Id
  private String id;
  private String name;
  private Long price;
  private Integer stock;
  private String status = "ACTIVE";

  public Product(String id, String name, Long price, Integer stock) {
    this(id, name, price, stock, "ACTIVE");
  }

  public Product(String id, String name, Long price, Integer stock, String status) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.stock = stock;
    this.status = status == null || status.isBlank() ? "ACTIVE" : status;
  }
}
