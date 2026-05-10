import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CheckoutPage from '../../components/CheckoutPage';
import * as orderService from '../../services/orderService';
import * as inventoryService from '../../services/inventoryService';

vi.mock('../../services/orderService');
vi.mock('../../services/inventoryService');

describe('Purchase Mock Tests', () => {
  const mockCart = {
    items: [
      { productId: 'P001', name: 'Laptop Dell', price: 15000000, quantity: 2 },
      { productId: 'P002', name: 'Mouse Logitech', price: 500000, quantity: 1 },
    ],
    total: 30500000,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('Mock: Đặt hàng thành công', async () => {
    vi.mocked(inventoryService.checkStock).mockResolvedValue({ available: true });
    vi.mocked(orderService.createOrder).mockResolvedValue({
      orderId: 'ORD-001',
      status: 'PENDING',
      totalPrice: 30550000,
      message: 'Đặt hàng thành công',
    });

    render(<CheckoutPage cart={mockCart} />);
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(orderService.createOrder).toHaveBeenCalledTimes(1);
      expect(inventoryService.checkStock).toHaveBeenCalledWith(
        expect.arrayContaining([
          expect.objectContaining({ productId: 'P001' }),
        ]),
      );
      expect(screen.getByText(/thành công/i)).toBeInTheDocument();
    });
  });

  test('Mock: Đặt hàng thất bại khi không đủ tồn kho', async () => {
    vi.mocked(inventoryService.checkStock).mockResolvedValue({
      available: false,
      message: 'Không đủ tồn kho',
    });

    render(<CheckoutPage cart={mockCart} />);
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(inventoryService.checkStock).toHaveBeenCalledTimes(1);
      expect(orderService.createOrder).not.toHaveBeenCalled();
      expect(screen.getByText(/không đủ tồn kho/i)).toBeInTheDocument();
    });
  });
});
