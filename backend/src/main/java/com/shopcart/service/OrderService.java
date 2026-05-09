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
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
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

    Long total = calculateOrderTotal(request);
    List<OrderItem> items = mapOrderItems(request.getItems());

    Order order = new Order();
    order.setItems(items);
    order.setUserId(request.getUserId());
    order.setStatus(OrderStatus.PENDING);
    order.setTotalPrice(total);

    Order saved = orderRepository.save(order);
    for (OrderItem item : items) {
      inventoryService.decreaseStock(item.getProductId(), item.getQuantity());
    }

    return OrderResponse.builder()
        .orderId(saved.getId())
        .status(saved.getStatus())
        .totalPrice(saved.getTotalPrice())
        .build();
  }

  public Long calculateOrderTotal(OrderRequest request) {
    validateOrderContent(request);

    long subtotal = 0L;
    for (OrderItemRequest itemRequest : request.getItems()) {
      long catalogPrice = getCatalogPrice(itemRequest.getProductId());
      subtotal += catalogPrice * itemRequest.getQuantity();
    }

    long discount = calculateDiscount(request.getCouponCode(), subtotal);
    long shipping = request.getShippingFee() == null ? 0L : request.getShippingFee();
    return subtotal - discount + shipping;
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
    Optional<Order> orderOptional = orderRepository.findById(id);
    if (orderOptional.isEmpty()) {
      return;
    }

    Order order = orderOptional.get();
    if (order.getItems() != null) {
      for (OrderItem item : order.getItems()) {
        inventoryService.increaseStock(item.getProductId(), item.getQuantity());
      }
    }
    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
  }

  public void cancelOrderForUser(String id, String userId) {
    Order order = getOrderForUser(id, userId);
    cancelOrder(order.getId());
  }

  private void validateCreateOrderRequest(OrderRequest request) {
    validateOrderContent(request);
    for (OrderItemRequest itemRequest : request.getItems()) {
      if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
        throw new IllegalArgumentException(ApiMessages.QUANTITY_MUST_BE_POSITIVE);
      }
      if (!inventoryService.isAvailable(itemRequest.getProductId(), itemRequest.getQuantity())) {
        throw new InsufficientStockException(ApiMessages.INSUFFICIENT_STOCK);
      }
    }
  }

  private void validateOrderContent(OrderRequest request) {
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new IllegalArgumentException(ApiMessages.ORDER_ITEMS_REQUIRED);
    }
    if (request.getShippingFee() != null && request.getShippingFee() < 0) {
      throw new IllegalArgumentException(ApiMessages.SHIPPING_FEE_NEGATIVE);
    }
  }

  private List<OrderItem> mapOrderItems(List<OrderItemRequest> itemRequests) {
    List<OrderItem> items = new ArrayList<>();
    for (OrderItemRequest itemRequest : itemRequests) {
      items.add(new OrderItem(itemRequest.getProductId(), itemRequest.getQuantity(), getCatalogPrice(itemRequest.getProductId())));
    }
    return items;
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

    if (coupon.getExpiryDate() != null && LocalDate.now().isAfter(LocalDate.parse(coupon.getExpiryDate()))) {
      throw new IllegalArgumentException(ApiMessages.COUPON_EXPIRED);
    }
    if (coupon.getMinOrderValue() != null && subtotal < coupon.getMinOrderValue()) {
      throw new IllegalArgumentException(ApiMessages.COUPON_MIN_ORDER_NOT_MET);
    }

    long discount = 0L;
    if ("PERCENT".equals(coupon.getDiscountType())) {
      discount = (long) (subtotal * (coupon.getDiscountValue() / 100.0));
    } else if ("FIXED".equals(coupon.getDiscountType()) || "FIXED_AMOUNT".equals(coupon.getDiscountType())) {
      discount = coupon.getDiscountValue();
    }
    return Math.min(discount, subtotal);
  }
}
