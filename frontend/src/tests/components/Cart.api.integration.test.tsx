import { beforeEach, describe, expect, test, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import axios from 'axios';
import CartComponent from '../../components/CartComponent';

vi.mock('axios');

const mockedAxios = vi.mocked(axios);
const authHeaders = { headers: { Authorization: 'Bearer token123' } };

describe('Cart Component + API Service Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC_API_1: Render giỏ hàng và gọi API khi người dùng cập nhật/xóa sản phẩm', async () => {
    const onCartCountChange = vi.fn();
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        items: [
          {
            productId: 'P001',
            productName: 'Laptop Dell',
            quantity: 2,
            price: 15000000,
          },
        ],
      },
    });
    mockedAxios.put.mockResolvedValueOnce({
      data: {
        items: [
          {
            productId: 'P001',
            productName: 'Laptop Dell',
            quantity: 3,
            price: 15000000,
          },
        ],
      },
    });
    mockedAxios.delete.mockResolvedValueOnce({ data: { items: [] } });

    render(<CartComponent userId="user01" onCartCountChange={onCartCountChange} />);

    expect(await screen.findByText('Laptop Dell')).toBeInTheDocument();
    expect(screen.getByTestId('cart-subtotal')).toHaveTextContent('30.000.000');
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/cart/user01', authHeaders);
    expect(onCartCountChange).toHaveBeenCalledWith(2);

    fireEvent.change(screen.getByTestId('quantity-input-P001'), { target: { value: '3' } });

    await waitFor(() => {
      expect(mockedAxios.put).toHaveBeenCalledWith(
        '/api/cart/update',
        { userId: 'user01', productId: 'P001', quantity: 3 },
        authHeaders,
      );
      expect(screen.getByTestId('cart-subtotal')).toHaveTextContent('45.000.000');
      expect(onCartCountChange).toHaveBeenCalledWith(3);
    });

    fireEvent.click(screen.getByTestId('delete-product-P001'));

    await waitFor(() => {
      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/cart/user01/P001', authHeaders);
      expect(screen.getByTestId('empty-cart-message')).toBeInTheDocument();
      expect(onCartCountChange).toHaveBeenCalledWith(0);
    });
  });

  test('TC_API_2: Submit đặt hàng gửi payload đúng API và hiển thị success message', async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        items: [
          {
            productId: 'P001',
            productName: 'Laptop Dell',
            quantity: 1,
            price: 15000000,
          },
        ],
      },
    });
    mockedAxios.post.mockResolvedValueOnce({ data: { orderId: 'ORD-API-001' } });
    mockedAxios.delete.mockResolvedValueOnce({ data: { items: [] } });

    render(<CartComponent userId="user01" />);

    await screen.findByText('Laptop Dell');
    fireEvent.change(screen.getByTestId('coupon-input'), { target: { value: 'sale10' } });
    fireEvent.click(screen.getByTestId('apply-coupon-btn'));

    expect(await screen.findByTestId('form-message')).toHaveTextContent('Đã áp dụng mã SALE10');
    expect(screen.getByTestId('checkout-total')).toHaveTextContent('13.550.000');

    fireEvent.change(screen.getByTestId('shipping-address-input'), {
      target: { value: '123 Nguyen Trai, TP HCM' },
    });
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/orders',
        {
          userId: 'user01',
          items: [
            {
              productId: 'P001',
              productName: 'Laptop Dell',
              quantity: 1,
              price: 15000000,
            },
          ],
          couponCode: 'SALE10',
          shippingFee: 50000,
          shippingAddress: '123 Nguyen Trai, TP HCM',
          paymentMethod: 'COD',
        },
        authHeaders,
      );
      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/cart/user01/P001', authHeaders);
      expect(screen.getByTestId('order-success')).toHaveTextContent(
        'Đặt hàng thành công: ORD-API-001',
      );
      expect(screen.getByTestId('empty-cart-message')).toBeInTheDocument();
    });
  });

  test('TC_API_3: Hiển thị lỗi API khi tải giỏ hàng thất bại', async () => {
    mockedAxios.get.mockRejectedValueOnce(new Error('network down'));

    render(<CartComponent userId="user01" />);

    expect(await screen.findByTestId('error-message')).toHaveTextContent('API Error');
  });

  test('TC_API_4: Hiển thị lỗi API từ service khi cập nhật hoặc đặt hàng thất bại', async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        items: [
          {
            productId: 'P001',
            productName: 'Laptop Dell',
            quantity: 1,
            price: 15000000,
          },
        ],
      },
    });
    mockedAxios.put.mockRejectedValueOnce({
      response: { data: { message: 'Số lượng vượt tồn kho' } },
    });
    mockedAxios.post.mockRejectedValueOnce({
      response: { data: { message: 'Không thể tạo đơn lúc này' } },
    });

    render(<CartComponent userId="user01" />);

    await screen.findByText('Laptop Dell');
    fireEvent.change(screen.getByTestId('quantity-input-P001'), { target: { value: '99' } });

    expect(await screen.findByTestId('form-message')).toHaveTextContent('Số lượng vượt tồn kho');

    fireEvent.change(screen.getByTestId('shipping-address-input'), {
      target: { value: '123 Nguyen Trai, TP HCM' },
    });
    fireEvent.click(screen.getByTestId('place-order-btn'));

    expect(await screen.findByTestId('form-message')).toHaveTextContent(
      'Không thể tạo đơn lúc này',
    );
  });
});
