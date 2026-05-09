import { describe, test, expect } from 'vitest';
import {
  calculateOrderPrice,
  checkInventoryAvailabilityForOrder as checkInventoryAvailability,
} from '../../utils/priceCalculation';

describe('priceCalculation', () => {
  describe('calculateOrderPrice', () => {
    test('calculateOrderPrice_TC1: Tính tổng giá trước giảm giá', () => {
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

    test('calculateOrderPrice_TC2: Áp dụng coupon giảm 10%', () => {
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

    test('calculateOrderPrice_TC3: Áp dụng coupon giảm 20%', () => {
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

    test('calculateOrderPrice_TC4: Áp dụng coupon giảm số tiền cố định', () => {
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

    test('calculateOrderPrice_TC5: Tính phí vận chuyển', () => {
      const items = [{ price: 10000000, quantity: 1 }];
      const result = calculateOrderPrice(items, undefined, 50000);

      expect(result.subtotal).toBe(10000000);
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(10050000);
    });

    test('calculateOrderPrice_TC6: Tính tổng cuối cùng', () => {
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

    test('calculateOrderPrice_TC7: Discount không vượt quá subtotal', () => {
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

    test('calculateOrderPrice_TC8: Coupon type không hỗ trợ không tạo discount', () => {
      const items = [{ price: 100000, quantity: 1 }];
      const coupon = {
        code: 'UNKNOWN',
        discountType: 'BOGO',
        discountValue: 50000,
      };
      const result = calculateOrderPrice(items, coupon as never, 0);

      expect(result.discount).toBe(0);
      expect(result.total).toBe(100000);
    });

    test('calculateOrderPrice_TC9: Giỏ hàng rỗng', () => {
      const items: Array<{ price: number; quantity: number }> = [];
      const result = calculateOrderPrice(items, undefined, 0);

      expect(result.subtotal).toBe(0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(0);
    });

    test('calculateOrderPrice_TC10: Nhiều sản phẩm với giảm giá % và shipping', () => {
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

  describe('checkInventoryAvailability', () => {
    test('checkInventoryAvailability_TC1: Tất cả sản phẩm còn hàng', () => {
      const items = [
        { productId: 'P001', quantity: 2, stock: 10 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(true);
      expect(result.unavailableProducts).toBeUndefined();
    });

    test('checkInventoryAvailability_TC2: Một sản phẩm hết hàng', () => {
      const items = [
        { productId: 'P001', quantity: 10, stock: 5 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(false);
      expect(result.unavailableProducts).toContain('P001');
      expect(result.unavailableProducts?.length).toBe(1);
    });

    test('checkInventoryAvailability_TC3: Nhiều sản phẩm hết hàng', () => {
      const items = [
        { productId: 'P001', quantity: 10, stock: 5 },
        { productId: 'P002', quantity: 10, stock: 3 },
        { productId: 'P003', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(false);
      expect(result.unavailableProducts).toContain('P001');
      expect(result.unavailableProducts).toContain('P002');
      expect(result.unavailableProducts?.length).toBe(2);
    });

    test('checkInventoryAvailability_TC4: Giỏ hàng rỗng', () => {
      const items: Array<{ productId: string; quantity: number; stock: number }> = [];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(true);
    });
  });
});
