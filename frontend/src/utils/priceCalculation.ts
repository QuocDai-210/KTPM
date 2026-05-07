// Frontend price calculation utility
export interface OrderItem {
  price: number;
  quantity: number;
}

export interface OrderPrice {
  subtotal: number;
  discount: number;
  shipping: number;
  total: number;
}

export interface CouponInfo {
  code: string;
  discountType: 'PERCENT' | 'FIXED';
  discountValue: number;
  minOrderValue?: number;
  expiryDate?: string;
}

export function calculateOrderPrice(
  items: OrderItem[],
  coupon?: CouponInfo,
  shipping: number = 0
): OrderPrice {
  // Calculate subtotal
  const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0);

  // Calculate discount based on coupon
  let discount = 0;
  if (coupon) {
    if (coupon.discountType === 'PERCENT') {
      discount = subtotal * (coupon.discountValue / 100);
    } else if (coupon.discountType === 'FIXED') {
      discount = coupon.discountValue;
    }
  }

  // Ensure discount doesn't exceed subtotal
  discount = Math.min(discount, subtotal);

  // Calculate total
  const total = subtotal - discount + shipping;

  return {
    subtotal,
    discount,
    shipping,
    total,
  };
}

export function validateCoupon(
  coupon: CouponInfo,
  orderValue: number
): { valid: boolean; message?: string } {
  // Check minimum order value
  if (coupon.minOrderValue && orderValue < coupon.minOrderValue) {
    return {
      valid: false,
      message: `Đơn hàng phải tối thiểu ${coupon.minOrderValue}đ`,
    };
  }

  // Check expiry date
  if (coupon.expiryDate) {
    const expiryDate = new Date(coupon.expiryDate);
    if (new Date() > expiryDate) {
      return {
        valid: false,
        message: 'Mã giảm giá đã hết hạn',
      };
    }
  }

  return { valid: true };
}

export function checkInventoryAvailabilityForOrder(
  items: Array<{ productId: string; quantity: number; stock: number }>
): { available: boolean; unavailableProducts?: string[] } {
  const unavailable = items
    .filter((item) => item.quantity > item.stock)
    .map((item) => item.productId);

  if (unavailable.length > 0) {
    return {
      available: false,
      unavailableProducts: unavailable,
    };
  }

  return { available: true };
}
