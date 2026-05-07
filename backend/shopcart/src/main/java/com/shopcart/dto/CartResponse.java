package com.shopcart.dto;

import com.shopcart.entity.CartItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
  private boolean success;
  private String message;
  private Long cartTotal;
  private List<CartItem> items;
  private Integer itemCount;
}
