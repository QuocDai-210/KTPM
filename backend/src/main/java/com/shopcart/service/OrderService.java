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
import com.shopcart.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final InventoryService inventoryService;
  private final ProductRepository productRepository;

  public OrderService(OrderRepository orderRepository, InventoryService inventoryService, ProductRepository productRepository) {
    this.orderRepository = orderRepository;
    this.inventoryService = inventoryService;
    this.productRepository = productRepository;
  }

  public OrderResponse createOrder(OrderRequest request) {
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm");
    }
    if (request.getShippingFee() != null && request.getShippingFee() < 0) {
      throw new IllegalArgumentException("Phí vận chuyển không được âm");
    }
    // Check inventory
    for (OrderItemRequest it : request.getItems()) {
      if (it.getQuantity() == null || it.getQuantity() < 1) {
        throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
      }
      if (!inventoryService.isAvailable(it.getProductId(), it.getQuantity())) {
        throw new InsufficientStockException("Tồn kho không đủ");
      }
    }

    Long total = calculateOrderTotal(request);

    Order order = new Order();
    // map items
    List<OrderItem> items = new ArrayList<>();
    for (OrderItemRequest it : request.getItems()) {
      long catalogPrice = productRepository.findById(it.getProductId())
          .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"))
          .getPrice();
      items.add(new OrderItem(it.getProductId(), it.getQuantity(), catalogPrice));
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
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm");
    }
    if (request.getShippingFee() != null && request.getShippingFee() < 0) {
      throw new IllegalArgumentException("Phí vận chuyển không được âm");
    }
    long subtotal = 0L;
    for (OrderItemRequest it : request.getItems()) {
      long catalogPrice = productRepository.findById(it.getProductId())
          .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"))
          .getPrice();
      subtotal += catalogPrice * it.getQuantity();
    }

    long discount = 0L;
    String couponCode = request.getCouponCode() == null ? null : request.getCouponCode().trim();
    if (couponCode != null && !couponCode.isBlank()) {
      Optional<Coupon> couponOpt = orderRepository.findCoupon(couponCode);
      if (couponOpt.isEmpty()) {
        throw new IllegalArgumentException("Mã giảm giá không tồn tại");
      }
      Coupon c = couponOpt.get();
      if (c.getExpiryDate() != null && java.time.LocalDate.now().isAfter(java.time.LocalDate.parse(c.getExpiryDate()))) {
        throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
      }
      if (c.getMinOrderValue() != null && subtotal < c.getMinOrderValue()) {
        throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu cho mã giảm giá");
      }
      if ("PERCENT".equals(c.getDiscountType())) {
        discount = (long) (subtotal * (c.getDiscountValue() / 100.0));
      } else if ("FIXED".equals(c.getDiscountType()) || "FIXED_AMOUNT".equals(c.getDiscountType())) {
        discount = c.getDiscountValue();
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
