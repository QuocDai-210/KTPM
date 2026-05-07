package com.shopcart.dto;

import com.shopcart.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
  private String orderId;
  private OrderStatus status;
  private Long totalPrice;
}
