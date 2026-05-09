import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from '../../App';
import * as cartService from '../../services/cartService';

vi.mock('../../services/cartService');

describe('Cart Mock Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.history.replaceState(null, '', '/');
    vi.mocked(cartService.getProducts).mockResolvedValue([
      { id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 },
    ]);
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
      cartTotal: 30000000,
      itemCount: 2,
    });
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
      render(<App />);

      // Simulate user input
      const quantityInput = await screen.findByTestId('quantity-input-P001');
      const addButton = screen.getByTestId('add-to-cart-P001');

      fireEvent.change(quantityInput, { target: { value: '2' } });
      fireEvent.click(addButton);

      // Assert
      await waitFor(() => {
        expect(cartService.addToCart).toHaveBeenCalledWith('user01', 'P001', 2);
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
      render(<App />);

      const quantityInput = await screen.findByTestId('quantity-input-P001');
      const addButton = screen.getByTestId('add-to-cart-P001');

      fireEvent.change(quantityInput, { target: { value: '50' } });
      fireEvent.click(addButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/chỉ còn 10 sản phẩm/i)).toBeInTheDocument();
        expect(cartService.addToCart).not.toHaveBeenCalled();
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

      render(<App />);

      const addButton = await screen.findByTestId('add-to-cart-P001');

      fireEvent.click(addButton);
      await waitFor(() => expect(cartService.addToCart).toHaveBeenCalledTimes(1));
      await screen.findByText('Laptop Dell');
      fireEvent.click(screen.getByRole('button', { name: 'Sản phẩm' }));
      const secondAddButton = await screen.findByTestId('add-to-cart-P001');
      fireEvent.click(secondAddButton);

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

      render(<App />);

      const addButton = await screen.findByTestId('add-to-cart-P001');
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(cartService.addToCart).toHaveBeenCalledWith(
          'user01',
          'P001',
          1,
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

      render(<App />);

      const addButton = await screen.findByTestId('add-to-cart-P001');
      fireEvent.click(addButton);

      await waitFor(() => {
        const badge = screen.getByTestId('cart-badge');
        expect(badge).toHaveTextContent('2');
      });
    });

    test('Mock: Badge dùng itemCount nếu response không có cartCount', async () => {
      vi.mocked(cartService.addToCart).mockResolvedValue({
        success: true,
        message: 'Success',
        itemCount: 4,
      });

      render(<App />);

      fireEvent.click(await screen.findByTestId('add-to-cart-P001'));

      await waitFor(() => {
        expect(screen.getByTestId('cart-badge')).toHaveTextContent('4');
      });
    });

    test('Mock: Badge fallback về quantity nếu response không có count', async () => {
      vi.mocked(cartService.addToCart).mockResolvedValue({
        success: true,
        message: 'Success',
      });

      render(<App />);

      const quantityInput = await screen.findByTestId('quantity-input-P001');
      fireEvent.change(quantityInput, { target: { value: '3' } });
      fireEvent.click(screen.getByTestId('add-to-cart-P001'));

      await waitFor(() => {
        expect(screen.getByTestId('cart-badge')).toHaveTextContent('3');
      });
    });

    test('Mock: Hiển thị lỗi khi service addToCart reject', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
      vi.mocked(cartService.addToCart).mockRejectedValue(new Error('network'));

      render(<App />);

      fireEvent.click(await screen.findByTestId('add-to-cart-P001'));

      await waitFor(() => {
        expect(screen.getByText('Không thể thêm sản phẩm vào giỏ')).toBeInTheDocument();
      });

      consoleSpy.mockRestore();
    });

    test('Mock: Người dùng mở tab giỏ hàng từ trang sản phẩm', async () => {
      render(<App />);

      await screen.findByTestId('add-to-cart-P001');
      fireEvent.click(screen.getByRole('button', { name: 'Giỏ hàng' }));

      await waitFor(() => {
        expect(screen.getByText('Laptop Dell')).toBeInTheDocument();
      });
    });
  });
});
