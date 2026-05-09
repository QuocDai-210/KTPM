import { describe, test, expect } from 'vitest';
import {
  validateCartItem,
  calculateCartTotal,
} from '../../utils/cartValidation';

describe('Cart Validation Tests', () => {
  describe('validateCartItem', () => {
    test('TC1: Quantity rỗng - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: '' as unknown as number,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng không được để trống');
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

    test('TC5: Quantity = 0 - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 0,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toBe('Số lượng phải lớn hơn 0');
    });

    test('TC6: Quantity vượt quá tồn kho - nên trả về lỗi', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 15,
        stock: 10,
      });
      expect(result.valid).toBe(false);
      expect(result.error).toContain('Số lượng vượt quá tồn kho');
    });

    test('TC7: Quantity hợp lệ - nên thành công', () => {
      const result = validateCartItem({
        productId: 'P001',
        quantity: 5,
        stock: 10,
      });
      expect(result.valid).toBe(true);
      expect(result.error).toBeUndefined();
    });

  });

  describe('calculateCartTotal', () => {
    test('TC1: Giỏ hàng rỗng', () => {
      const items: Array<{ price: number; quantity: number }> = [];
      const result = calculateCartTotal(items, undefined, undefined, 0);

      expect(result.subtotal).toBe(0);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(0);
    });

    test('TC2: Tính tổng giá đúng với nhiều sản phẩm', () => {
      const items = [
        { price: 15000000, quantity: 2 },
        { price: 500000, quantity: 1 },
      ];
      const result = calculateCartTotal(items, undefined, undefined, 0);

      expect(result.subtotal).toBe(30500000);
      expect(result.discount).toBe(0);
      expect(result.total).toBe(30500000);
    });

    test('TC3: Áp dụng mã giảm giá', () => {
      const items = [{ price: 1000000, quantity: 2 }];
      const result = calculateCartTotal(items, 10, undefined, 0);

      expect(result.subtotal).toBe(2000000);
      expect(result.discount).toBe(200000);
      expect(result.total).toBe(1800000);
    });

    test('TC4: Tính lại tổng giá sau khi xóa sản phẩm', () => {
      const itemsAfterRemove = [{ price: 15000000, quantity: 2 }];
      const result = calculateCartTotal(itemsAfterRemove, undefined, undefined, 0);

      expect(result.subtotal).toBe(30000000);
      expect(result.total).toBe(30000000);
    });
  });
});
