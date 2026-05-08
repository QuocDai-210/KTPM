package com.shopcart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
  @Id
  private String productId;
  private String userId;
  private Integer quantity;
  private String productName;
  private Long price;

  public CartItem(String productId, Integer quantity, String productName, Long price) {
    this.productId = productId;
    this.quantity = quantity;
    this.productName = productName;
    this.price = price;
  }
}
