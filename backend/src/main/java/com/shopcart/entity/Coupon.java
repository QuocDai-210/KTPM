package com.shopcart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
  private String code;
  private Long discountValue; // percent
  private Long minOrderValue;
  private String expiryDate; // ISO date
}
