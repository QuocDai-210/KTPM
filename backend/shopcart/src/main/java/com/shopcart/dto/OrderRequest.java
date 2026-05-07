package com.shopcart.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
  private String userId;
  private List<OrderItemRequest> items;
  private String couponCode;
  private Long shippingFee;
  private String shippingAddress;
  private String paymentMethod;
}
