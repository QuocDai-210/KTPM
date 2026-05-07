package com.shopcart.service;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.Coupon;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.OrderStatus;
import com.shopcart.exception.InsufficientStockException;
import com.shopcart.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final InventoryService inventoryService;

  public OrderService(OrderRepository orderRepository, InventoryService inventoryService) {
    this.orderRepository = orderRepository;
    this.inventoryService = inventoryService;
  }

  public OrderResponse createOrder(OrderRequest request) {
    // Check inventory
    for (OrderItemRequest it : request.getItems()) {
      if (!inventoryService.isAvailable(it.getProductId(), it.getQuantity())) {
        throw new InsufficientStockException("Tồn kho không đủ");
      }
    }

    Long total = calculateOrderTotal(request);

    Order order = new Order();
    // map items
    List<OrderItem> items = new ArrayList<>();
    for (OrderItemRequest it : request.getItems()) {
      items.add(new OrderItem(it.getProductId(), it.getQuantity(), it.getPrice()));
    }
    order.setItems(items);
    order.setUserId(request.getUserId());
    order.setStatus(OrderStatus.PENDING);
    order.setTotalPrice(total);

    Order saved = orderRepository.save(order);

    // decrease stock
    for (OrderItem it : items) {
      inventoryService.decreaseStock(it.getProductId(), it.getQuantity());
    }

    return OrderResponse.builder().orderId(saved.getId()).status(saved.getStatus()).totalPrice(saved.getTotalPrice()).build();
  }

  public Long calculateOrderTotal(OrderRequest request) {
    long subtotal = 0L;
    for (OrderItemRequest it : request.getItems()) {
      subtotal += it.getPrice() * it.getQuantity();
    }

    long discount = 0L;
    if (request.getCouponCode() != null) {
      Optional<Coupon> couponOpt = orderRepository.findCoupon(request.getCouponCode());
      if (couponOpt.isPresent()) {
        Coupon c = couponOpt.get();
        if ("PERCENT".equals(c.getDiscountType())) {
          discount = (long) (subtotal * (c.getDiscountValue() / 100.0));
        } else if ("FIXED".equals(c.getDiscountType())) {
          discount = c.getDiscountValue();
        }
      }
    } else if (request.getCouponCode() == null) {
      // No coupon
    }

    discount = Math.min(discount, subtotal);

    long shipping = request.getShippingFee() == null ? 0L : request.getShippingFee();
    return subtotal - discount + shipping;
  }

  public Order getOrderById(String id) {
    return orderRepository.findById(id).orElse(null);
  }

  public void cancelOrder(String id) {
    Optional<Order> opt = orderRepository.findById(id);
    if (opt.isEmpty()) return;
    Order order = opt.get();
    if (order.getItems() != null) {
      for (OrderItem it : order.getItems()) {
        inventoryService.increaseStock(it.getProductId(), it.getQuantity());
      }
    }
    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
  }
}
