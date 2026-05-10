package com.shopcart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Inventory {
  @Id private String productId;

  private Integer quantity;

  public Inventory(String productId, Integer quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }
}
