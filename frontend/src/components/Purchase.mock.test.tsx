import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CheckoutPage from './CheckoutPage';
import * as orderService from '../services/orderService';
import * as inventoryService from '../services/inventoryService';

vi.mock('../services/orderService');
vi.mock('../services/inventoryService');

describe('Purchase Mock Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Mock: Checkout Success', () => {
    test('Mock: Đặt hàng thành công với mocked services', async () => {
      // Arrange
      const mockCheckoutResponse = {
        orderId: 'ORD-20260509-001',
        status: 'PENDING',
        totalPrice: 30550000,
        message: 'Đặt hàng thành công',
      };

      const mockCartData = {
        items: [
          { productId: 'P001', name: 'Laptop Dell', price: 15000000, quantity: 2 },
          { productId: 'P002', name: 'Mouse Logitech', price: 500000, quantity: 1 },
        ],
        total: 30500000,
      };

      vi.mocked(inventoryService.checkStock).mockResolvedValue({
        available: true,
        message: 'Stock available',
      });

      vi.mocked(orderService.createOrder).mockResolvedValue(mockCheckoutResponse);

      // Act
      render(<CheckoutPage cart={mockCartData} />);

      const placeOrderBtn = screen.getByTestId('place-order-btn');
      fireEvent.click(placeOrderBtn);

      // Assert
      await waitFor(() => {
        expect(inventoryService.checkStock).toHaveBeenCalled();
        expect(orderService.createOrder).toHaveBeenCalled();
        expect(screen.getByText(/thành công/i)).toBeInTheDocument();
      });
    });
  });

  describe('Mock: Apply Coupon', () => {
    test('Mock: Áp dụng mã giảm giá thành công', async () => {
      const mockCartData = {
        items: [
          { productId: 'P001', name: 'Laptop', price: 15000000, quantity: 2 },
        ],
        total: 30000000,
      };

      const mockPriceResponse = {
        subtotal: 30000000,
        discount: 3000000, // 10%
        shipping: 50000,
        total: 27050000,
      };

      vi.mocked(orderService.applyCoupon).mockResolvedValue(mockPriceResponse);

      // Act
      render(<CheckoutPage cart={mockCartData} />);

      const couponInput = screen.getByTestId('coupon-input');
      const applyCouponBtn = screen.getByTestId('apply-coupon-btn');

      fireEvent.change(couponInput, { target: { value: 'SALE10' } });
      fireEvent.click(applyCouponBtn);

      // Assert
      await waitFor(() => {
        expect(orderService.applyCoupon).toHaveBeenCalledWith('SALE10');
        expect(screen.getByTestId('discount-display')).toHaveTextContent('3.000.000');
        expect(screen.getByTestId('total-display')).toHaveTextContent('27.050.000');
      });
    });
  });

  describe('Mock: Out of Stock', () => {
    test('Mock: Đặt hàng thất bại - Hết hàng', async () => {
      const mockCartData = {
        items: [
          { productId: 'P001', name: 'Laptop', price: 15000000, quantity: 5 },
        ],
        total: 75000000,
      };

      const mockStockError = {
        available: false,
        message: 'Không đủ tồn kho cho sản phẩm P001. Chỉ còn 3 chiếc.',
      };

      vi.mocked(inventoryService.checkStock).mockResolvedValue(mockStockError);

      // Act
      render(<CheckoutPage cart={mockCartData} />);

      const placeOrderBtn = screen.getByTestId('place-order-btn');
      fireEvent.click(placeOrderBtn);

      // Assert
      await waitFor(() => {
        expect(inventoryService.checkStock).toHaveBeenCalled();
        expect(orderService.createOrder).not.toHaveBeenCalled();
        expect(screen.getByText(/không đủ tồn kho/i)).toBeInTheDocument();
      });
    });
  });

  describe('Mock: Verify Order Payload', () => {
    test('Mock: Xác minh dữ liệu order được gửi chính xác', async () => {
      const mockCheckoutResponse = {
        orderId: 'ORD-20260509-001',
        status: 'PENDING',
        totalPrice: 30550000,
      };

      vi.mocked(inventoryService.checkStock).mockResolvedValue({ available: true });
      vi.mocked(orderService.createOrder).mockResolvedValue(mockCheckoutResponse);

      const mockCartData = {
        items: [
          { productId: 'P001', name: 'Laptop', price: 15000000, quantity: 2 },
        ],
        total: 30000000,
      };

      render(<CheckoutPage cart={mockCartData} />);

      const placeOrderBtn = screen.getByTestId('place-order-btn');
      fireEvent.click(placeOrderBtn);

      await waitFor(() => {
        expect(orderService.createOrder).toHaveBeenCalledWith(
          expect.objectContaining({
            items: expect.any(Array),
            totalPrice: expect.any(Number),
            shippingAddress: expect.any(Object),
          })
        );
      });
    });
  });

  describe('Mock: Multiple Coupons', () => {
    test('Mock: Kiểm tra không thể áp dụng 2 coupon cùng lúc', async () => {
      const mockCartData = {
        items: [{ productId: 'P001', name: 'Laptop', price: 15000000, quantity: 2 }],
        total: 30000000,
      };

      vi.mocked(orderService.applyCoupon)
        .mockResolvedValueOnce({
          subtotal: 30000000,
          discount: 3000000,
          shipping: 50000,
          total: 27050000,
        })
        .mockRejectedValueOnce({
          message: 'Chỉ được áp dụng 1 mã giảm giá',
        });

      render(<CheckoutPage cart={mockCartData} />);

      const couponInput = screen.getByTestId('coupon-input');
      const applyCouponBtn = screen.getByTestId('apply-coupon-btn');

      // First coupon
      fireEvent.change(couponInput, { target: { value: 'SALE10' } });
      fireEvent.click(applyCouponBtn);

      await waitFor(() => {
        expect(orderService.applyCoupon).toHaveBeenCalledTimes(1);
      });

      // Try second coupon
      fireEvent.change(couponInput, { target: { value: 'SAVE500' } });
      fireEvent.click(applyCouponBtn);

      await waitFor(() => {
        expect(orderService.applyCoupon).toHaveBeenCalledTimes(2);
        expect(screen.getByText(/chỉ được áp dụng 1 mã/i)).toBeInTheDocument();
      });
    });
  });

  describe('Mock: Price Calculation', () => {
    test('Mock: Xác minh tính toán giá chính xác', async () => {
      const mockCartData = {
        items: [
          { productId: 'P001', name: 'Laptop', price: 15000000, quantity: 2 },
          { productId: 'P002', name: 'Mouse', price: 500000, quantity: 1 },
        ],
        total: 30500000,
      };

      const mockPriceResponse = {
        subtotal: 30500000,
        discount: 3050000, // 10%
        shipping: 50000,
        total: 27500000,
      };

      vi.mocked(orderService.calculatePrice).mockResolvedValue(mockPriceResponse);

      render(<CheckoutPage cart={mockCartData} />);

      fireEvent.click(screen.getByTestId('apply-coupon-btn'));

      await waitFor(() => {
        expect(screen.getByTestId('subtotal-display')).toHaveTextContent('30.500.000');
        expect(screen.getByTestId('discount-display')).toHaveTextContent('3.050.000');
        expect(screen.getByTestId('shipping-display')).toHaveTextContent('50.000');
        expect(screen.getByTestId('total-display')).toHaveTextContent('27.500.000');
      });
    });
  });
});
