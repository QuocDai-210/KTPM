import { describe, test, expect } from 'vitest';
import {
  validateCartItem,
  calculateCartTotal,
  checkInventoryAvailability,
} from './cartValidation';

describe('Cart Validation Tests', () => {
  describe('validateCartItem', () => {
    test('TC1: Quantity = 0 - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 0,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng phải lớn hơn 0');
    });

    test('TC2: Quantity = null - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: null as unknown as number,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng không được để trống');
    });

    test('TC3: Quantity = undefined - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: undefined as unknown as number,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng không được để trống');
    });

    test('TC4: Quantity âm - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: -5,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng phải lớn hơn 0');
    });

    test('TC5: Quantity vượt quá tồn kho - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 15,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toContain('Số lượng vượt quá tồn kho');
    });

    test('TC6: Quantity hợp lệ - nên thành công', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 5,
        stock: 10,
      });
      expect(result.valid).toBe(true);
      expect(result.error).toBeUndefined();
    });

    test('TC7: Quantity = 1 (min valid) - nên thành công', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 1,
        stock: 10,
      });
      expect(result.valid).toBe(true);
    });

    test('TC8: Quantity = stock (boundary) - nên thành công', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 10,
        stock: 10,
      });
      expect(result.valid).toBe(true);
    });

    test('TC9: ProductId rỗng - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: '',
        quantity: 5,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Product ID không được để trống');
    });

    test('TC10: Quantity không phải số nguyên - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 2.5,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng phải là số nguyên');
    });
  });

  describe('calculateCartTotal', () => {
    test('TC11: Tính tổng giá không có giảm giá', () => {
      const items = [
        { price: 15000000, quantity: 2 },
        { price: 500000, quantity: 1 },
      ];
      const result = calculateCartTotal(items, undefined, undefined, 50000);

      expect(result.subtotal).toBe(30500000);
      expect(result.discount).toBe(0);
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(30550000);
    });

    test('TC12: Tính tổng giá với giảm giá 10%', () => {
      const items = [{ price: 1000000, quantity: 2 }];
      const result = calculateCartTotal(items, 10, undefined, 0);

      expect(result.subtotal).toBe(2000000);
      expect(result.discount).toBe(200000);
      expect(result.total).toBe(1800000);
    });

    test('TC13: Tính tổng giá với giảm giá cố định', () => {
      const items = [{ price: 1000000, quantity: 1 }];
      const result = calculateCartTotal(items, undefined, 100000, 0);

      expect(result.subtotal).toBe(1000000);
      expect(result.discount).toBe(100000);
      expect(result.total).toBe(900000);
    });

    test('TC14: Giỏ hàng rỗng', () => {
      const items: Array<{ price: number; quantity: number }> = [];
      const result = calculateCartTotal(items, undefined, undefined, 0);

      expect(result.subtotal).toBe(0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(0);
    });

    test('TC15: Discount không vượt quá subtotal', () => {
      const items = [{ price: 100000, quantity: 1 }];
      const result = calculateCartTotal(items, undefined, 200000, 0);

      // Discount capped at subtotal
      expect(result.discount).toBe(100000);
      expect(result.total).toBe(0);
    });

    test('TC16: Tính giá với shipping', () => {
      const items = [{ price: 1000000, quantity: 1 }];
      const result = calculateCartTotal(items, undefined, undefined, 50000);

      expect(result.subtotal).toBe(1000000);
      expect(result.shipping).toBe(50000);
      expect(result.total).toBe(1050000);
    });

    test('TC17: Tính giá đầy đủ: subtotal + shipping - discount', () => {
      const items = [{ price: 1000000, quantity: 1 }];
      const result = calculateCartTotal(items, 20, undefined, 100000);

      expect(result.subtotal).toBe(1000000);
      expect(result.discount).toBe(200000);
      expect(result.shipping).toBe(100000);
      expect(result.total).toBe(900000);
    });
  });

  describe('checkInventoryAvailability', () => {
    test('TC18: Tất cả sản phẩm còn hàng - available = true', () => {
      const items = [
        { productId: 'P001', quantity: 2, stock: 10 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(true);
      expect(result.message).toBeUndefined();
    });

    test('TC19: Một sản phẩm vượt tồn kho - available = false', () => {
      const items = [
        { productId: 'P001', quantity: 10, stock: 5 },
        { productId: 'P002', quantity: 1, stock: 5 },
      ];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(false);
      expect(result.message).toContain('P001');
    });

    test('TC20: Giỏ hàng rỗng - available = true', () => {
      const items: Array<{ productId: string; quantity: number; stock: number }> = [];
      const result = checkInventoryAvailability(items);

      expect(result.available).toBe(true);
    });
  });
});
