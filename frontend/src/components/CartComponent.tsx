import { useEffect, useState } from 'react';
import * as cartService from '../services/cartService';

interface Item {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}

export default function CartComponent({ userId }: { userId: string }) {
  const [items, setItems] = useState<Item[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [couponCode, setCouponCode] = useState('');
  const [shippingAddress, setShippingAddress] = useState('');
  const [orderMessage, setOrderMessage] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    cartService
      .getCart(userId)
      .then((r) => {
        if (active) {
          setItems(Array.isArray(r.items) ? r.items : []);
          setError(null);
        }
      })
      .catch(() => {
        if (active) {
          setError('API Error');
          setItems([]);
        }
      });
    return () => {
      active = false;
    };
  }, [userId]);

  if (error) {
    return <div data-testid="error-message">{error}</div>;
  }

  if (items === null) {
    return <div data-testid="loading-spinner">Loading...</div>;
  }

  if (items.length === 0) {
    return (
      <div>
        {orderMessage && <div data-testid="order-success">{orderMessage}</div>}
        <div data-testid="empty-cart-message">Giỏ hàng trống</div>
      </div>
    );
  }

  const subtotal = items.reduce((s, it) => s + it.price * it.quantity, 0);
  const shippingFee = items.length > 0 ? 50000 : 0;
  const orderTotal = subtotal + shippingFee;

  const handleDelete = (productId: string) => {
    cartService.removeFromCart(userId, productId).then((r) => setItems(Array.isArray(r.items) ? r.items : []));
  };

  const handleChange = (productId: string, value: string) => {
    const q = parseInt(value || '0', 10);
    if (Number.isNaN(q)) return;
    cartService.updateQuantity(userId, productId, q).then((r) => setItems(Array.isArray(r.items) ? r.items : []));
  };

  const handleCheckout = async () => {
    setOrderMessage(null);
    if (!shippingAddress.trim()) {
      setError('Vui lòng nhập địa chỉ giao hàng');
      return;
    }

    try {
      const order = await cartService.createOrder({
        userId,
        items,
        couponCode: couponCode.trim() || undefined,
        shippingFee,
        shippingAddress,
        paymentMethod: 'COD',
      });
      setError(null);
      setOrderMessage(`Đặt hàng thành công: ${order.orderId}`);
      setItems([]);
    } catch {
      setError('Không thể đặt hàng');
    }
  };

  return (
    <div>
      {items.map((it) => (
        <div key={it.productId}>
          <div>{it.productName}</div>
          <input data-testid={`quantity-input-${it.productId}`} value={it.quantity} onChange={(e) => handleChange(it.productId, e.target.value)} />
          <button data-testid={`delete-product-${it.productId}`} onClick={() => handleDelete(it.productId)}>Delete</button>
        </div>
      ))}
      <div data-testid="cart-subtotal">{new Intl.NumberFormat('vi-VN').format(subtotal)}</div>
      <div data-testid="shipping-fee">{new Intl.NumberFormat('vi-VN').format(shippingFee)}</div>
      <div data-testid="cart-total">{new Intl.NumberFormat('vi-VN').format(subtotal)}</div>
      <div data-testid="checkout-total">{new Intl.NumberFormat('vi-VN').format(orderTotal)}</div>
      <input
        data-testid="coupon-input"
        placeholder="Mã giảm giá"
        value={couponCode}
        onChange={(e) => setCouponCode(e.target.value)}
      />
      <input
        data-testid="shipping-address-input"
        placeholder="Địa chỉ giao hàng"
        value={shippingAddress}
        onChange={(e) => setShippingAddress(e.target.value)}
      />
      <button data-testid="place-order-btn" onClick={handleCheckout}>Đặt hàng</button>
      {orderMessage && <div data-testid="order-success">{orderMessage}</div>}
    </div>
  );
}
