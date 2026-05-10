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

  test('Mock: cartService.addToCart', async () => {
    vi.mocked(cartService.addToCart).mockResolvedValue({
      success: true,
      message: 'Them vao gio hang thanh cong',
      cartTotal: 30000000,
    });

    render(<App />);

    fireEvent.click(await screen.findByTestId('add-to-cart-P001'));

    await waitFor(() => {
      expect(cartService.addToCart).toHaveBeenCalledWith(
        'user01',
        expect.any(String),
        expect.any(Number),
      );
      expect(screen.getByText(/thanh cong/i)).toBeInTheDocument();
    });
  });

  test('Mock: them san pham that bai khi service tra loi loi', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    vi.mocked(cartService.addToCart).mockRejectedValue({
      success: false,
      message: 'Khong the them vao gio hang',
      error: 'API_ERROR',
    });

    render(<App />);

    const quantityInput = await screen.findByTestId('quantity-input-P001');
    const addButton = screen.getByTestId('add-to-cart-P001');

    fireEvent.change(quantityInput, { target: { value: '2' } });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(cartService.addToCart).toHaveBeenCalledWith('user01', 'P001', 2);
      expect(screen.getByText('Không thể thêm sản phẩm vào giỏ')).toBeInTheDocument();
    });

    consoleSpy.mockRestore();
  });

  test('Mock: xac minh addToCart duoc goi dung so lan', async () => {
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
    fireEvent.click(screen.getByRole('button', { name: 'San pham' }));

    const secondAddButton = await screen.findByTestId('add-to-cart-P001');
    fireEvent.click(secondAddButton);

    await waitFor(() => {
      expect(cartService.addToCart).toHaveBeenCalledTimes(2);
    });
  });

  test('Mock: xac minh arguments duoc truyen chinh xac', async () => {
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
      expect(cartService.addToCart).toHaveBeenCalledWith('user01', 'P001', 1);
    });
  });

  test('Mock: badge gio hang cap nhat sau khi add', async () => {
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
      expect(screen.getByTestId('cart-badge')).toHaveTextContent('2');
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
