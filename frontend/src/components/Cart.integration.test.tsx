import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CartComponent from './CartComponent';
import * as cartService from '../services/cartService';

vi.mock('../services/cartService');

describe('Cart Component Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC1: Hiển thị giỏ hàng rỗng khi chưa có sản phẩm', async () => {
    // Arrange
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [],
      total: 0,
    });

    // Act
    render(<CartComponent userId="user01" />);

    // Assert
    await waitFor(() => {
      expect(screen.getByTestId('empty-cart-message')).toBeInTheDocument();
    });
  });

  test('TC2: Hiển thị danh sách sản phẩm trong giỏ hàng', async () => {
    // Arrange
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

    // Act
    render(<CartComponent userId="user01" />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
      expect(screen.getByText('Mouse Logitech')).toBeInTheDocument();
      expect(screen.getByTestId('cart-total')).toHaveTextContent('30.500.000');
    });
  });

  test('TC3: Xóa sản phẩm khỏi giỏ hàng', async () => {
    // Arrange
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

    // Act
    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
    });

    const deleteBtn = screen.getByTestId('delete-product-P001');
    fireEvent.click(deleteBtn);

    // Assert
    await waitFor(() => {
      expect(cartService.removeFromCart).toHaveBeenCalledWith('user01', 'P001');
    });
  });

  test('TC4: Cập nhật số lượng sản phẩm trong giỏ', async () => {
    // Arrange
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

    // Act
    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
    });

    const quantityInput = screen.getByTestId('quantity-input-P001');
    fireEvent.change(quantityInput, { target: { value: '5' } });

    // Assert
    await waitFor(() => {
      expect(cartService.updateQuantity).toHaveBeenCalledWith('user01', 'P001', 5);
    });
  });

  test('TC5: Hiển thị thông báo lỗi khi API gặp sự cố', async () => {
    // Arrange
    vi.mocked(cartService.getCart).mockRejectedValue(
      new Error('API Error')
    );

    // Act
    render(<CartComponent userId="user01" />);

    // Assert
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
  });

  test('TC6: Hiển thị loading state khi tải giỏ hàng', async () => {
    // Arrange
    vi.mocked(cartService.getCart).mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve({
        items: [],
        total: 0,
      }), 100))
    );

    // Act
    render(<CartComponent userId="user01" />);

    // Assert
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument();
    });
  });
});
