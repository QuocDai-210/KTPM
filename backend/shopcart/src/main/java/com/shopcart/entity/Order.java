package com.shopcart.entity;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Order {
  private String id;
  private String userId;
  private List<OrderItem> items;
  private OrderStatus status;
  private Long totalPrice;

  public void setItems(List<OrderItem> items) {
    this.items = items;
  }
}
