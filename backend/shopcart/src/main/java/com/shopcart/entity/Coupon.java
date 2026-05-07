package com.shopcart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
  private String code;
  private String discountType; // PERCENT | FIXED
  private Long discountValue;
  private Long minOrderValue;
  private String expiryDate; // ISO date
}
