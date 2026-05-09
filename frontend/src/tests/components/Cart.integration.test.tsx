import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CartComponent from '../../components/CartComponent';
import * as cartService from '../../services/cartService';

vi.mock('../../services/cartService');

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

  test('TC1b: Hiển thị giỏ hàng rỗng khi API không trả mảng items', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      success: true,
    });

    render(<CartComponent userId="user01" />);

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

  test('TC3b: Xóa sản phẩm xử lý response không có items', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
    });
    vi.mocked(cartService.removeFromCart).mockResolvedValue({ success: true });

    render(<CartComponent userId="user01" />);

    await screen.findByText('Laptop Dell');
    fireEvent.click(screen.getByTestId('delete-product-P001'));

    expect(await screen.findByTestId('empty-cart-message')).toBeInTheDocument();
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

  test('TC4b: Cập nhật số lượng xử lý response không có items', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
    });
    vi.mocked(cartService.updateQuantity).mockResolvedValue({ success: true });

    render(<CartComponent userId="user01" />);

    const quantityInput = await screen.findByTestId('quantity-input-P001');
    fireEvent.change(quantityInput, { target: { value: '3' } });

    expect(await screen.findByTestId('empty-cart-message')).toBeInTheDocument();
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

  test('TC6b: Không set state nếu unmount trước khi getCart resolve', async () => {
    let resolveCart: (value: { items: cartService.CartItem[] }) => void = () => undefined;
    vi.mocked(cartService.getCart).mockImplementation(
      () => new Promise((resolve) => {
        resolveCart = resolve;
      }),
    );

    const { unmount } = render(<CartComponent userId="user01" />);
    unmount();
    resolveCart({ items: [] });

    await waitFor(() => {
      expect(screen.queryByTestId('empty-cart-message')).not.toBeInTheDocument();
    });
  });

  test('TC6c: Không set state nếu unmount trước khi getCart reject', async () => {
    let rejectCart: (error: Error) => void = () => undefined;
    vi.mocked(cartService.getCart).mockImplementation(
      () => new Promise((_, reject) => {
        rejectCart = reject;
      }),
    );

    const { unmount } = render(<CartComponent userId="user01" />);
    unmount();
    rejectCart(new Error('offline'));

    await waitFor(() => {
      expect(screen.queryByTestId('error-message')).not.toBeInTheDocument();
    });
  });

  test('TC7: Chặn số lượng không hợp lệ khi cập nhật giỏ', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
    });

    render(<CartComponent userId="user01" />);

    const quantityInput = await screen.findByTestId('quantity-input-P001');
    fireEvent.change(quantityInput, { target: { value: '' } });
    expect(await screen.findByTestId('form-message')).toHaveTextContent('Số lượng phải lớn hơn 0');

    fireEvent.change(quantityInput, { target: { value: '0' } });

    expect(await screen.findByTestId('form-message')).toHaveTextContent('Số lượng phải lớn hơn 0');
    expect(cartService.updateQuantity).not.toHaveBeenCalled();
  });

  test('TC8: Áp dụng coupon hợp lệ và không hợp lệ', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 2,
          price: 15000000,
        },
      ],
    });

    render(<CartComponent userId="user01" />);

    await screen.findByText('Laptop Dell');
    fireEvent.change(screen.getByTestId('coupon-input'), { target: { value: 'bad-code' } });
    fireEvent.click(screen.getByTestId('apply-coupon-btn'));
    expect(await screen.findByTestId('form-message')).toHaveTextContent('Mã giảm giá không hợp lệ');

    fireEvent.change(screen.getByTestId('coupon-input'), { target: { value: ' fixed100k ' } });
    fireEvent.click(screen.getByTestId('apply-coupon-btn'));
    expect(await screen.findByTestId('form-message')).toHaveTextContent('Đã áp dụng mã FIXED100K');
    expect(screen.getByTestId('checkout-total')).toHaveTextContent('29.950.000');
  });

  test('TC9: Checkout yêu cầu địa chỉ, đặt hàng thành công và xóa giỏ', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 1,
          price: 15000000,
        },
      ],
    });
    vi.mocked(cartService.createOrder).mockResolvedValue({ orderId: 'ORD-001' });

    render(<CartComponent userId="user01" />);

    await screen.findByText('Laptop Dell');
    fireEvent.click(screen.getByTestId('place-order-btn'));
    expect(await screen.findByTestId('form-message')).toHaveTextContent('Vui lòng nhập địa chỉ giao hàng');

    fireEvent.change(screen.getByTestId('shipping-address-input'), { target: { value: '123 Nguyen Trai' } });
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(cartService.createOrder).toHaveBeenCalledWith(expect.objectContaining({
        userId: 'user01',
        shippingAddress: '123 Nguyen Trai',
        paymentMethod: 'COD',
      }));
      expect(screen.getByTestId('empty-cart-message')).toBeInTheDocument();
      expect(screen.getByTestId('order-success')).toHaveTextContent('ORD-001');
    });
  });

  test('TC10: Checkout hiển thị lỗi khi tạo đơn thất bại', async () => {
    vi.mocked(cartService.getCart).mockResolvedValue({
      items: [
        {
          productId: 'P001',
          productName: 'Laptop Dell',
          quantity: 1,
          price: 15000000,
        },
      ],
    });
    vi.mocked(cartService.createOrder).mockRejectedValue(new Error('server'));

    render(<CartComponent userId="user01" />);

    await screen.findByText('Laptop Dell');
    fireEvent.change(screen.getByTestId('shipping-address-input'), { target: { value: '123 Nguyen Trai' } });
    fireEvent.click(screen.getByTestId('place-order-btn'));

    expect(await screen.findByTestId('form-message')).toHaveTextContent('Không thể đặt hàng');
  });
});
