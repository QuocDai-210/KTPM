package com.shopcart.service;

import com.shopcart.common.ApiMessages;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.OrderStatus;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private static final String ORDER_REQUIRED = "Đơn hàng không được để trống";
  private static final String SHIPPING_ADDRESS_REQUIRED = "Địa chỉ giao hàng không được để trống";
  private static final String PAYMENT_METHOD_REQUIRED = "Phương thức thanh toán không được để trống";
  private static final String ORDER_ITEM_REQUIRED = "Sản phẩm trong đơn hàng không được để trống";
  private static final String PRODUCT_ID_REQUIRED = "Product ID không được rỗng";
  private static final String QUANTITY_REQUIRED = "Số lượng phải lớn hơn 0";

  private final OrderRepository orderRepository;
  private final InventoryService inventoryService;
  private final ProductRepository productRepository;

  public OrderService(
      OrderRepository orderRepository,
      InventoryService inventoryService,
      ProductRepository productRepository) {
    this.orderRepository = orderRepository;
    this.inventoryService = inventoryService;
    this.productRepository = productRepository;
  }

  public OrderResponse createOrder(OrderRequest request) {
    validateCreateOrderRequest(request);

    List<OrderItem> items = mapOrderItems(request.getItems());
    long total = calculateTotalPrice(items, request.getCouponCode(), request.getShippingFee());

    Order saved = orderRepository.save(buildPendingOrder(request.getUserId(), items, total));
    decreaseStock(items);

    return toResponse(saved);
  }

  public Long calculateOrderTotal(OrderRequest request) {
    validateOrderRequest(request, false);
    validateShippingFee(request.getShippingFee());

    long subtotal = calculateSubtotal(request.getItems());
    return subtotal
        - calculateDiscount(request.getCouponCode(), subtotal)
        + shippingFeeOrZero(request.getShippingFee());
  }

  public Order getOrderById(String id) {
    return orderRepository.findById(id).orElse(null);
  }

  public Order getOrderForUser(String id, String userId) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException(ApiMessages.ORDER_NOT_FOUND));

    if (order.getUserId() != null && !order.getUserId().equals(userId)) {
      throw new AccessDeniedException(ApiMessages.ORDER_ACCESS_FORBIDDEN);
    }
    return order;
  }

  public void cancelOrder(String id) {
    orderRepository
        .findById(id)
        .ifPresent(
            order -> {
              restoreStock(order.getItems());
              order.setStatus(OrderStatus.CANCELLED);
              orderRepository.save(order);
            });
  }

  public void cancelOrderForUser(String id, String userId) {
    cancelOrder(getOrderForUser(id, userId).getId());
  }

  private void validateCreateOrderRequest(OrderRequest request) {
    validateOrderRequest(request, true);
    validateShippingFee(request.getShippingFee());
    checkStockBeforeOrder(request.getItems());
  }

  private void validateOrderRequest(OrderRequest request, boolean requireCheckoutInfo) {
    if (request == null) {
      throw new IllegalArgumentException(ORDER_REQUIRED);
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new IllegalArgumentException(ApiMessages.ORDER_ITEMS_REQUIRED);
    }
    if (requireCheckoutInfo) {
      requireText(request.getShippingAddress(), SHIPPING_ADDRESS_REQUIRED);
      requireText(request.getPaymentMethod(), PAYMENT_METHOD_REQUIRED);
    }
    request.getItems().forEach(this::validateOrderItem);
  }

  private void validateOrderItem(OrderItemRequest item) {
    if (item == null) {
      throw new IllegalArgumentException(ORDER_ITEM_REQUIRED);
    }
    requireText(item.getProductId(), PRODUCT_ID_REQUIRED);
    if (item.getQuantity() == null || item.getQuantity() < 1) {
      throw new IllegalArgumentException(QUANTITY_REQUIRED);
    }
  }

  private void requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }

  private void validateShippingFee(Long shippingFee) {
    if (shippingFee != null && shippingFee < 0) {
      throw new IllegalArgumentException(ApiMessages.SHIPPING_FEE_NEGATIVE);
    }
  }

  private void checkStockBeforeOrder(List<OrderItemRequest> items) {
    for (OrderItemRequest item : items) {
      if (!inventoryService.isAvailable(item.getProductId(), item.getQuantity())) {
        throw new InsufficientStockException(ApiMessages.INSUFFICIENT_STOCK);
      }
    }
  }

  private List<OrderItem> mapOrderItems(List<OrderItemRequest> itemRequests) {
    List<OrderItem> items = new ArrayList<>();
    for (OrderItemRequest itemRequest : itemRequests) {
      items.add(
          new OrderItem(
              itemRequest.getProductId(),
              itemRequest.getQuantity(),
              getCatalogPrice(itemRequest.getProductId())));
    }
    return items;
  }

  private long calculateSubtotal(List<OrderItemRequest> items) {
    long subtotal = 0L;
    for (OrderItemRequest item : items) {
      subtotal += getCatalogPrice(item.getProductId()) * item.getQuantity();
    }
    return subtotal;
  }

  private long calculateTotalPrice(List<OrderItem> items, String couponCode, Long shippingFee) {
    long subtotal = 0L;
    for (OrderItem item : items) {
      subtotal += item.getPrice() * item.getQuantity();
    }
    return subtotal - calculateDiscount(couponCode, subtotal) + shippingFeeOrZero(shippingFee);
  }

  private long shippingFeeOrZero(Long shippingFee) {
    return shippingFee == null ? 0L : shippingFee;
  }

  private long getCatalogPrice(String productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new IllegalArgumentException(ApiMessages.PRODUCT_NOT_FOUND))
        .getPrice();
  }

  private long calculateDiscount(String couponCode, long subtotal) {
    if (couponCode == null || couponCode.trim().isBlank()) {
      return 0L;
    }

    Coupon coupon =
        orderRepository
            .findCoupon(couponCode.trim())
            .orElseThrow(() -> new IllegalArgumentException(ApiMessages.COUPON_NOT_FOUND));

    validateCoupon(coupon, subtotal);
    return Math.min(discountValue(coupon, subtotal), subtotal);
  }

  private void validateCoupon(Coupon coupon, long subtotal) {
    if (coupon.getExpiryDate() != null
        && LocalDate.now().isAfter(LocalDate.parse(coupon.getExpiryDate()))) {
      throw new IllegalArgumentException(ApiMessages.COUPON_EXPIRED);
    }
    if (coupon.getMinOrderValue() != null && subtotal < coupon.getMinOrderValue()) {
      throw new IllegalArgumentException(ApiMessages.COUPON_MIN_ORDER_NOT_MET);
    }
  }

  private long discountValue(Coupon coupon, long subtotal) {
    if ("PERCENT".equals(coupon.getDiscountType())) {
      return (long) (subtotal * (coupon.getDiscountValue() / 100.0));
    }
    if ("FIXED".equals(coupon.getDiscountType())
        || "FIXED_AMOUNT".equals(coupon.getDiscountType())) {
      return coupon.getDiscountValue();
    }
    return 0L;
  }

  private Order buildPendingOrder(String userId, List<OrderItem> items, long totalPrice) {
    Order order = new Order();
    order.setUserId(userId);
    order.setItems(items);
    order.setStatus(OrderStatus.PENDING);
    order.setTotalPrice(totalPrice);
    return order;
  }

  private void decreaseStock(List<OrderItem> items) {
    for (OrderItem item : items) {
      inventoryService.decreaseStock(item.getProductId(), item.getQuantity());
    }
  }

  private void restoreStock(List<OrderItem> items) {
    if (items == null) {
      return;
    }
    for (OrderItem item : items) {
      inventoryService.increaseStock(item.getProductId(), item.getQuantity());
    }
  }

  private OrderResponse toResponse(Order order) {
    return OrderResponse.builder()
        .orderId(order.getId())
        .status(order.getStatus())
        .totalPrice(order.getTotalPrice())
        .build();
  }
}
