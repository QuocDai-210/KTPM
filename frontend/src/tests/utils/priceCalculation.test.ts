import { describe, test, expect } from 'vitest';
import {
  calculateOrderPrice,
  checkInventoryAvailability,
} from '../../utils/priceCalculation';

describe('Price Calculation Tests', () => {
  describe('calculateOrderPrice', () => {
    test('TC1: Tính tổng giá trước giảm giá', () => {
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

    test('TC3: Áp dụng coupon giảm số tiền cố định', () => {
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

    test('TC4: Tính phí vận chuyển', () => {
      const items = [{ price: 10000000, quantity: 1 }];
      const result = calculateOrderPrice(items, undefined, 50000);

      expect(result.subtotal).toBe(10000000);
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(10050000);
    });

    test('TC5: Tính tổng cuối cùng', () => {
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

  });

  describe('checkInventoryAvailability', () => {
    test('TC1: Kiểm tra sản phẩm không đủ tồn kho', () => {
      const items = [
        { productId: 'P001', quantity: 10, stock: 5 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(false);
      expect(result.unavailableProducts).toContain('P001');
      expect(result.unavailableProducts?.length).toBe(1);
    });
  });
});
