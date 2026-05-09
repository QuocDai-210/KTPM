import { beforeEach, describe, expect, test, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import ProductsComponent from './ProductsComponent';
import * as cartService from '../services/cartService';

vi.mock('../services/cartService');

describe('ProductsComponent coverage tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('hiển thị loading rồi render danh sách sản phẩm từ API', async () => {
    vi.mocked(cartService.getProducts).mockResolvedValue([
      { id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 },
      { id: 'P002', name: 'Mouse Logitech', price: 500000, stock: 0 },
    ]);
    const onAddToCart = vi.fn();

    render(<ProductsComponent userId="user01" onAddToCart={onAddToCart} />);

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    expect(await screen.findByText('Laptop Dell')).toBeInTheDocument();
    expect(screen.getByTestId('add-to-cart-P002')).toBeDisabled();

    fireEvent.click(screen.getByTestId('add-to-cart-P001'));

    expect(onAddToCart).toHaveBeenCalledWith('P001', 1);
    expect(screen.getByTestId('quantity-input-P001')).toHaveValue(1);
  });

  test('dùng fallback products khi API trả sai định dạng', async () => {
    vi.mocked(cartService.getProducts).mockResolvedValue({ not: 'array' } as unknown as cartService.Product[]);

    render(<ProductsComponent userId="user01" />);

    expect(await screen.findByText('Laptop Dell XPS 13')).toBeInTheDocument();
    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
  });

  test('dùng fallback products và thông báo khi API lỗi', async () => {
    vi.mocked(cartService.getProducts).mockRejectedValue(new Error('offline'));

    render(<ProductsComponent userId="user01" />);

    expect(await screen.findByTestId('error-message')).toHaveTextContent('Đang dùng dữ liệu mẫu');
    expect(screen.getByText('USB-C Cable')).toBeInTheDocument();
  });

  test('chặn số lượng âm, bằng 0 và vượt tồn kho', async () => {
    vi.mocked(cartService.getProducts).mockResolvedValue([
      { id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 },
    ]);
    const onAddToCart = vi.fn();

    render(<ProductsComponent userId="user01" onAddToCart={onAddToCart} />);

    const quantityInput = await screen.findByTestId('quantity-input-P001');
    fireEvent.change(quantityInput, { target: { value: '-1' } });
    expect(quantityInput).toHaveValue(1);

    fireEvent.change(quantityInput, { target: { value: '0' } });
    fireEvent.click(screen.getByTestId('add-to-cart-P001'));
    expect(screen.getByTestId('error-message')).toHaveTextContent('Số lượng phải lớn hơn 0');
    expect(onAddToCart).not.toHaveBeenCalled();

    fireEvent.change(quantityInput, { target: { value: '11' } });
    fireEvent.click(screen.getByTestId('add-to-cart-P001'));
    expect(screen.getByTestId('error-message')).toHaveTextContent('Chỉ còn 10 sản phẩm');
    expect(onAddToCart).not.toHaveBeenCalled();
  });

  test('không set state sau khi component unmount trước khi API resolve', async () => {
    let resolveProducts: (products: cartService.Product[]) => void = () => undefined;
    vi.mocked(cartService.getProducts).mockImplementation(
      () => new Promise((resolve) => {
        resolveProducts = resolve;
      }),
    );

    const { unmount } = render(<ProductsComponent userId="user01" />);
    unmount();
    resolveProducts([{ id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 }]);

    await waitFor(() => {
      expect(screen.queryByText('Laptop Dell')).not.toBeInTheDocument();
    });
  });

  test('không set state sau khi component unmount trước khi API reject', async () => {
    let rejectProducts: (error: Error) => void = () => undefined;
    vi.mocked(cartService.getProducts).mockImplementation(
      () => new Promise((_, reject) => {
        rejectProducts = reject;
      }),
    );

    const { unmount } = render(<ProductsComponent userId="user01" />);
    unmount();
    rejectProducts(new Error('offline'));

    await waitFor(() => {
      expect(screen.queryByTestId('error-message')).not.toBeInTheDocument();
    });
  });
});
