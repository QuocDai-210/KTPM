import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CartComponent from './CartComponent';
import * as cartService from '../services/cartService';

vi.mock('../services/cartService');

describe('Cart Component Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC1: hien thi gio hang rong khi chua co san pham', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [],
      total: 0,
    });

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('empty-cart-message')).toBeInTheDocument();
    });
  });

  test('TC2: hien thi danh sach san pham trong gio hang', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
        {
          productId: 'P002',
          productName: 'Mouse Logitech',
          quantity: 1,
          price: 500000,
        },
      ],
      total: 30500000,
    });

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
      expect(screen.getByText('Mouse Logitech')).toBeInTheDocument();
      expect(screen.getByTestId('cart-total')).toHaveTextContent('30.500.000');
    });
  });

  test('TC3: xoa san pham khoi gio hang', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
      total: 30000000,
    });

    vi.mocked(cartService.removeFromCart).mockResolvedValue({
      items: [],
      total: 0,
    });

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId('delete-product-P001'));

    await waitFor(() => {
      expect(cartService.removeFromCart).toHaveBeenCalledWith('user01', 'P001');
    });
  });

  test('TC4: cap nhat so luong san pham trong gio', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
      total: 30000000,
    });

    vi.mocked(cartService.updateQuantity).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 5,
          price: 15000000,
        },
      ],
      total: 75000000,
    });

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByTestId('quantity-input-P001'), { target: { value: '5' } });

    await waitFor(() => {
      expect(cartService.updateQuantity).toHaveBeenCalledWith('user01', 'P001', 5);
    });
  });

  test('TC5: hien thi thong bao loi khi API gap su co', async () => {
    vi.mocked(cartService.getCart).mockRejectedValue(new Error('API Error'));

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
  });

  test('TC6: hien thi loading state khi tai gio hang', async () => {
    vi.mocked(cartService.getCart).mockImplementation(
      () =>
        new Promise((resolve) =>
          setTimeout(
            () =>
              resolve({
                items: [],
                total: 0,
              }),
            100,
          ),
        ),
    );

    render(<CartComponent userId="user01" />);

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument();
    });
  });

  test('TC7: checkout xoa hoan toan san pham da thanh toan khoi gio hang', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 1,
          price: 15000000,
        },
        {
          productId: 'P002',
          productName: 'Mouse Logitech',
          quantity: 1,
          price: 500000,
        },
      ],
      total: 15500000,
    });

    vi.mocked(cartService.createOrder).mockResolvedValue({
      orderId: 'ORD-001',
      status: 'PENDING',
      totalPrice: 15550000,
    });

    vi.mocked(cartService.removeFromCart)
      .mockResolvedValueOnce({
        items: [{ productId: 'P002', productName: 'Mouse Logitech', quantity: 1, price: 500000 }],
      })
      .mockResolvedValueOnce({ items: [] });

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByTestId('shipping-address-input'), {
      target: { value: '123 Test Street' },
    });
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(cartService.createOrder).toHaveBeenCalled();
      expect(cartService.removeFromCart).toHaveBeenCalledWith('user01', 'P001');
      expect(cartService.removeFromCart).toHaveBeenCalledWith('user01', 'P002');
      expect(screen.getByTestId('empty-cart-message')).toBeInTheDocument();
    });
  });
});
