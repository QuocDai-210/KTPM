import { useMemo, useState } from 'react';
import * as orderService from '../services/orderService';
import * as inventoryService from '../services/inventoryService';

type CheckoutItem = {
  productId: string;
  name: string;
  price: number;
  quantity: number;
};

type CheckoutCart = {
  items: CheckoutItem[];
  total: number;
};

type CheckoutPageProps = {
  cart: CheckoutCart;
};

type PriceState = {
  subtotal: number;
  discount: number;
  shipping: number;
  total: number;
};

const formatMoney = (value: number) => new Intl.NumberFormat('vi-VN').format(value);

export default function CheckoutPage({ cart }: CheckoutPageProps) {
  const initialSubtotal = useMemo(
    () => cart.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [cart.items],
  );
  const [couponCode, setCouponCode] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [price, setPrice] = useState<PriceState>({
    subtotal: initialSubtotal,
    discount: 0,
    shipping: 50000,
    total: initialSubtotal + 50000,
  });

  const applyCoupon = async () => {
    setMessage(null);
    try {
      if (couponCode.trim()) {
        const resp = await orderService.applyCoupon(couponCode.trim());
        setPrice(resp);
        setMessage('Áp dụng mã thành công');
        return;
      }

      const fallback = await orderService.calculatePrice(
        cart.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
      );
      setPrice(fallback);
    } catch (error) {
      const err = error as { message?: string };
      setMessage(err.message ?? 'Không thể áp dụng mã giảm giá');
    }
  };

  const placeOrder = async () => {
    setMessage(null);
    const stock = await inventoryService.checkStock(
      cart.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
    );

    if (!stock.available) {
      setMessage(stock.message ?? 'Không đủ tồn kho');
      return;
    }

    try {
      const response = await orderService.createOrder({
        userId: 'user01',
        items: cart.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
        totalPrice: price.total,
        shippingFee: price.shipping,
        shippingAddress: {
          line1: '123 Test Street',
          city: 'HCM',
        },
        paymentMethod: 'COD',
      });
      setMessage(response.message ?? `Đặt hàng thành công: ${response.orderId}`);
    } catch {
      setMessage('Không thể đặt hàng');
    }
  };

  return (
    <section>
      <h2>Checkout</h2>

      <div data-testid="subtotal-display">{formatMoney(price.subtotal)}</div>
      <div data-testid="discount-display">{formatMoney(price.discount)}</div>
      <div data-testid="shipping-display">{formatMoney(price.shipping)}</div>
      <div data-testid="total-display">{formatMoney(price.total)}</div>

      <input
        data-testid="coupon-input"
        value={couponCode}
        onChange={(e) => setCouponCode(e.target.value)}
        placeholder="Nhập mã giảm giá"
      />
      <button data-testid="apply-coupon-btn" onClick={applyCoupon}>
        Áp dụng
      </button>
      <button data-testid="place-order-btn" onClick={placeOrder}>
        Đặt hàng
      </button>

      {message && <div>{message}</div>}
    </section>
  );
}
