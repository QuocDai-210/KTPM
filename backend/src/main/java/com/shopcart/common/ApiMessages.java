package com.shopcart.common;

public final class ApiMessages {
  public static final String INVALID_AUTHORIZATION = "Authorization header is invalid";
  public static final String CART_ACCESS_FORBIDDEN = "Cannot access another user's cart";
  public static final String ORDER_ACCESS_FORBIDDEN = "Cannot access another user's order";
  public static final String ORDER_OPERATION_FORBIDDEN = "Cannot modify another user's order";

  public static final String PRODUCT_ID_REQUIRED = "Product ID is required";
  public static final String PRODUCT_NOT_FOUND = "Product does not exist";
  public static final String PRODUCT_INACTIVE = "Product is no longer available";
  public static final String QUANTITY_MUST_BE_POSITIVE = "Quantity must be greater than 0";
  public static final String INSUFFICIENT_STOCK = "Insufficient stock";

  public static final String CART_ADD_SUCCESS = "Added item to cart successfully";
  public static final String CART_REMOVE_SUCCESS = "Removed item from cart successfully";
  public static final String CART_ITEM_NOT_FOUND = "Product is not present in the cart";
  public static final String CART_UPDATE_SUCCESS = "Updated cart quantity successfully";

  public static final String ORDER_ITEMS_REQUIRED = "Order must contain at least one item";
  public static final String SHIPPING_FEE_NEGATIVE = "Shipping fee cannot be negative";
  public static final String COUPON_NOT_FOUND = "Coupon code does not exist";
  public static final String COUPON_EXPIRED = "Coupon code has expired";
  public static final String COUPON_MIN_ORDER_NOT_MET = "Order does not meet the coupon minimum value";
  public static final String ORDER_NOT_FOUND = "Order not found";

  private ApiMessages() {}
}
