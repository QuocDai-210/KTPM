import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CartComponent from './CartComponent';
import * as cartService from '../services/cartService';

vi.mock('../services/cartService');

describe('Cart Mock Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Mock: Add to Cart Success', () => {
    test('Mock: Thêm sản phẩm thành công với mocked service', async () => {
      // Arrange
      const mockResponse = {
        success: true,
        message: 'Thêm vào giỏ hàng thành công',
        cartTotal: 30000000,
        cartCount: 2,
      };

      vi.mocked(cartService.addToCart).mockResolvedValue(mockResponse);

      // Act
      render(<CartComponent userId="user01" />);

      // Simulate user input
      const quantityInput = screen.getByTestId('quantity-input');
      const addButton = screen.getByTestId('add-to-cart-btn');

      fireEvent.change(quantityInput, { target: { value: '2' } });
      fireEvent.click(addButton);

      // Assert
      await waitFor(() => {
        expect(cartService.addToCart).toHaveBeenCalledWith('user01', expect.objectContaining({
          productId: expect.any(String),
          quantity: 2,
        }));
        expect(screen.getByText(/thành công/i)).toBeInTheDocument();
      });
    });
  });

  describe('Mock: Add to Cart Failure', () => {
    test('Mock: Thêm sản phẩm thất bại - Stock không đủ', async () => {
      // Arrange
      const mockError = {
        success: false,
        message: 'Số lượng không được vượt quá tồn kho',
        error: 'INSUFFICIENT_STOCK',
      };

      vi.mocked(cartService.addToCart).mockRejectedValue(mockError);

      // Act
      render(<CartComponent userId="user01" />);

      const quantityInput = screen.getByTestId('quantity-input');
      const addButton = screen.getByTestId('add-to-cart-btn');

      fireEvent.change(quantityInput, { target: { value: '50' } });
      fireEvent.click(addButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/vượt quá tồn kho/i)).toBeInTheDocument();
      });
    });
  });

  describe('Mock: Verify Call Count', () => {
    test('Mock: Xác minh addToCart được gọi đúng số lần', async () => {
      vi.mocked(cartService.addToCart).mockResolvedValue({
        success: true,
        message: 'Success',
        cartTotal: 30000000,
        cartCount: 1,
      });

      render(<CartComponent userId="user01" />);

      const addButton = screen.getByTestId('add-to-cart-btn');

      fireEvent.click(addButton);
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(cartService.addToCart).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Mock: Verify Call Arguments', () => {
    test('Mock: Xác minh arguments được truyền chính xác', async () => {
      vi.mocked(cartService.addToCart).mockResolvedValue({
        success: true,
        message: 'Success',
        cartTotal: 30000000,
        cartCount: 1,
      });

      render(<CartComponent userId="user01" />);

      const addButton = screen.getByTestId('add-to-cart-btn');
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(cartService.addToCart).toHaveBeenCalledWith(
          'user01',
          expect.objectContaining({
            productId: expect.any(String),
            quantity: expect.any(Number),
          })
        );
      });
    });
  });

  describe('Mock: Cart Badge Update', () => {
    test('Mock: Badge giỏ hàng cập nhật sau khi add', async () => {
      vi.mocked(cartService.addToCart).mockResolvedValue({
        success: true,
        message: 'Success',
        cartTotal: 30000000,
        cartCount: 2,
      });

      render(<CartComponent userId="user01" />);

      const addButton = screen.getByTestId('add-to-cart-btn');
      fireEvent.click(addButton);

      await waitFor(() => {
        const badge = screen.getByTestId('cart-badge');
        expect(badge).toHaveTextContent('2');
      });
    });
  });
});
