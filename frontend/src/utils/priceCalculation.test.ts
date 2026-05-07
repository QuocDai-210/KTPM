import { describe, test, expect } from 'vitest';
import {
  calculateOrderPrice,
  validateCoupon,
  checkInventoryAvailabilityForOrder,
} from './priceCalculation';

describe('Price Calculation Tests', () => {
  describe('calculateOrderPrice', () => {
    test('TC1: Tính tổng giá không có giảm giá', () => {
      const items = [
        { price: 15000000, quantity: 2 },
        { price: 500000, quantity: 1 },
      ];
      const result = calculateOrderPrice(items, null as unknown as undefined, 50000);

      expect(result.subtotal).toBe(30500000);
      expect(result.discount).toBe(0);
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(30550000);
    });

    test('TC2: Áp dụng coupon giảm 10%', () => {
      const items = [
        { price: 15000000, quantity: 2 },
        { price: 500000, quantity: 1 },
      ];
      const coupon = {
        code: 'SALE10',
        discountType: 'PERCENT' as const,
        discountValue: 10,
      };
      const result = calculateOrderPrice(items, coupon, 50000);

      expect(result.subtotal).toBe(30500000);
      expect(result.discount).toBe(3050000); // 10%
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(27500000);
    });

    test('TC3: Áp dụng coupon giảm 20%', () => {
      const items = [{ price: 10000000, quantity: 1 }];
      const coupon = {
        code: 'SALE20',
        discountType: 'PERCENT' as const,
        discountValue: 20,
      };
      const result = calculateOrderPrice(items, coupon, 0);

      expect(result.subtotal).toBe(10000000);
      expect(result.discount).toBe(2000000); // 20%
      expect(result.total).toBe(8000000);
    });

    test('TC4: Áp dụng coupon giảm số tiền cố định', () => {
      const items = [{ price: 10000000, quantity: 1 }];
      const coupon = {
        code: 'FIXED100K',
        discountType: 'FIXED' as const,
        discountValue: 100000,
      };
      const result = calculateOrderPrice(items, coupon, 0);

      expect(result.subtotal).toBe(10000000);
      expect(result.discount).toBe(100000);
      expect(result.total).toBe(9900000);
    });

    test('TC5: Tính phí vận chuyển', () => {
      const items = [{ price: 10000000, quantity: 1 }];
      const result = calculateOrderPrice(items, undefined, 50000);

      expect(result.subtotal).toBe(10000000);
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(10050000);
    });

    test('TC6: Tính giá đầy đủ: subtotal + shipping - discount', () => {
      const items = [{ price: 10000000, quantity: 1 }];
      const coupon = {
        code: 'SALE10',
        discountType: 'PERCENT' as const,
        discountValue: 10,
      };
      const result = calculateOrderPrice(items, coupon, 50000);

      expect(result.subtotal).toBe(10000000);
      expect(result.discount).toBe(1000000); // 10%
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(9050000); // 10M - 1M + 50K
    });

    test('TC7: Discount không vượt quá subtotal', () => {
      const items = [{ price: 100000, quantity: 1 }];
      const coupon = {
        code: 'HUGE',
        discountType: 'FIXED' as const,
        discountValue: 500000, // Vượt quá subtotal
      };
      const result = calculateOrderPrice(items, coupon, 0);

      expect(result.subtotal).toBe(100000);
      expect(result.discount).toBe(100000); // Capped at subtotal
      expect(result.total).toBe(0);
    });

    test('TC8: Giỏ hàng rỗng', () => {
      const items: Array<{ price: number; quantity: number }> = [];
      const result = calculateOrderPrice(items, undefined, 0);

      expect(result.subtotal).toBe(0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(0);
    });

    test('TC9: Nhiều sản phẩm với giảm giá % và shipping', () => {
      const items = [
        { price: 1000000, quantity: 3 },
        { price: 500000, quantity: 2 },
      ];
      const coupon = {
        code: 'SALE15',
        discountType: 'PERCENT' as const,
        discountValue: 15,
      };
      const result = calculateOrderPrice(items, coupon, 100000);

      expect(result.subtotal).toBe(4000000);
      expect(result.discount).toBe(600000); // 15%
      expect(result.shipping).toBe(100000);
      expect(result.total).toBe(3500000);
    });
  });

  describe('validateCoupon', () => {
    test('TC10: Coupon hợp lệ', () => {
      const coupon = {
        code: 'VALID10',
        discountType: 'PERCENT' as const,
        discountValue: 10,
      };
      const result = validateCoupon(coupon, 1000000);

      expect(result.valid).toBe(true);
      expect(result.message).toBeUndefined();
    });

    test('TC11: Đơn hàng thấp hơn minimum order value', () => {
      const coupon = {
        code: 'MINORDER',
        discountType: 'FIXED' as const,
        discountValue: 100000,
        minOrderValue: 5000000,
      };
      const result = validateCoupon(coupon, 1000000);

      expect(result.valid).toBe(false);
      expect(result.message).toContain('5000000');
    });

    test('TC12: Mã giảm giá hết hạn', () => {
      const coupon = {
        code: 'EXPIRED',
        discountType: 'FIXED' as const,
        discountValue: 100000,
        expiryDate: '2023-12-31', // Quá khứ
      };
      const result = validateCoupon(coupon, 1000000);

      expect(result.valid).toBe(false);
      expect(result.message).toContain('hết hạn');
    });

    test('TC13: Mã giảm giá chưa hết hạn', () => {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 30);
      
      const coupon = {
        code: 'VALID',
        discountType: 'FIXED' as const,
        discountValue: 100000,
        expiryDate: futureDate.toISOString().split('T')[0],
      };
      const result = validateCoupon(coupon, 1000000);

      expect(result.valid).toBe(true);
    });
  });

  describe('checkInventoryAvailabilityForOrder', () => {
    test('TC14: Tất cả sản phẩm còn hàng', () => {
      const items = [
        { productId: 'P001', quantity: 2, stock: 10 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailabilityForOrder(items);

      expect(result.available).toBe(true);
      expect(result.unavailableProducts).toBeUndefined();
    });

    test('TC15: Một sản phẩm hết hàng', () => {
      const items = [
        { productId: 'P001', quantity: 10, stock: 5 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailabilityForOrder(items);

      expect(result.available).toBe(false);
      expect(result.unavailableProducts).toContain('P001');
      expect(result.unavailableProducts?.length).toBe(1);
    });

    test('TC16: Nhiều sản phẩm hết hàng', () => {
      const items = [
        { productId: 'P001', quantity: 10, stock: 5 },
        { productId: 'P002', quantity: 10, stock: 3 },
        { productId: 'P003', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailabilityForOrder(items);

      expect(result.available).toBe(false);
      expect(result.unavailableProducts).toContain('P001');
      expect(result.unavailableProducts).toContain('P002');
      expect(result.unavailableProducts?.length).toBe(2);
    });

    test('TC17: Giỏ hàng rỗng', () => {
      const items: Array<{ productId: string; quantity: number; stock: number }> = [];
      const result = checkInventoryAvailabilityForOrder(items);

      expect(result.available).toBe(true);
    });
  });
});
