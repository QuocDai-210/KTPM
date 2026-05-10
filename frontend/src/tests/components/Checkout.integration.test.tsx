import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CheckoutPage from '../../components/CheckoutPage';
import * as orderService from '../../services/orderService';
import * as inventoryService from '../../services/inventoryService';

vi.mock('../../services/orderService');
vi.mock('../../services/inventoryService');

describe('Checkout Component Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('a) Test CheckoutSummary component voi du lieu gio hang', () => {
    test('TC1: Hien thi tong gia chinh xac', async () => {
      const mockCart = {
        items: [
          { productId: 'P001', name: 'Laptop Dell', price: 15000000, quantity: 2 },
          { productId: 'P002', name: 'Mouse Logitech', price: 500000, quantity: 1 },
        ],
        total: 30500000,
      };

      render(<CheckoutPage cart={mockCart} />);

      await waitFor(() => {
        expect(screen.getByTestId('subtotal-display')).toHaveTextContent('30.500.000');
        expect(screen.getByTestId('shipping-display')).toHaveTextContent('50.000');
        expect(screen.getByTestId('total-display')).toHaveTextContent('30.550.000');
      });
    });
  });

  describe('b) Test PriceCalculator component tinh gia real-time', () => {
    test('TC2: Cap nhat gia khi ap dung coupon', async () => {
      vi.mocked(orderService.applyCoupon).mockResolvedValue({
        subtotal: 30500000,
        discount: 3050000,
        shipping: 50000,
        total: 27500000,
      });

      render(
        <CheckoutPage
          cart={{
            items: [
              { productId: 'P001', name: 'Laptop Dell', price: 15000000, quantity: 2 },
              { productId: 'P002', name: 'Mouse Logitech', price: 500000, quantity: 1 },
            ],
            total: 30500000,
          }}
        />,
      );

      fireEvent.change(screen.getByTestId('coupon-input'), {
        target: { value: 'SALE10' },
      });
      fireEvent.click(screen.getByTestId('apply-coupon-btn'));

      await waitFor(() => {
        expect(orderService.applyCoupon).toHaveBeenCalledWith('SALE10');
        expect(screen.getByText('Áp dụng mã thành công')).toBeInTheDocument();
        expect(screen.getByTestId('subtotal-display')).toHaveTextContent('30.500.000');
        expect(screen.getByTestId('discount-display')).toHaveTextContent('3.050.000');
        expect(screen.getByTestId('shipping-display')).toHaveTextContent('50.000');
        expect(screen.getByTestId('total-display')).toHaveTextContent('27.500.000');
      });
    });

    test('TC3: Hien thi loi khi coupon khong hop le', async () => {
      vi.mocked(orderService.applyCoupon).mockRejectedValue({
        message: 'Mã giảm giá không hợp lệ',
      });

      render(
        <CheckoutPage
          cart={{
            items: [{ productId: 'P001', name: 'Laptop Dell', price: 15000000, quantity: 1 }],
            total: 15000000,
          }}
        />,
      );

      fireEvent.change(screen.getByTestId('coupon-input'), {
        target: { value: 'INVALID' },
      });
      fireEvent.click(screen.getByTestId('apply-coupon-btn'));

      await waitFor(() => {
        expect(orderService.applyCoupon).toHaveBeenCalledWith('INVALID');
        expect(screen.getByText('Mã giảm giá không hợp lệ')).toBeInTheDocument();
      });
    });
  });

  describe('c) Test InventoryWarning component canh bao het hang', () => {
    test('TC4: Hien thi canh bao khi ton kho khong du', async () => {
      vi.mocked(inventoryService.checkStock).mockResolvedValue({
        available: false,
        message: 'Không đủ tồn kho cho sản phẩm P001',
      });

      render(
        <CheckoutPage
          cart={{
            items: [
              { productId: 'P001', name: 'Laptop Dell', price: 15000000, quantity: 2 },
              { productId: 'P002', name: 'Mouse Logitech', price: 500000, quantity: 1 },
            ],
            total: 30500000,
          }}
        />,
      );

      fireEvent.click(screen.getByTestId('place-order-btn'));

      await waitFor(() => {
        expect(inventoryService.checkStock).toHaveBeenCalledWith([
          { productId: 'P001', quantity: 2 },
          { productId: 'P002', quantity: 1 },
        ]);
        expect(orderService.createOrder).not.toHaveBeenCalled();
        expect(screen.getByText('Không đủ tồn kho cho sản phẩm P001')).toBeInTheDocument();
      });
    });
  });
});
