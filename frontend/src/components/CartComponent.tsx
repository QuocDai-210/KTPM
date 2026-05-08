import { useEffect, useMemo, useState } from 'react';
import * as cartService from '../services/cartService';
import { calculateOrderPrice, type CouponInfo } from '../utils/priceCalculation';

const couponCatalog: Record<string, CouponInfo> = {
  SALE10: { code: 'SALE10', discountType: 'PERCENT', discountValue: 10 },
  SALE20: { code: 'SALE20', discountType: 'PERCENT', discountValue: 20 },
  FIXED100K: { code: 'FIXED100K', discountType: 'FIXED', discountValue: 100000 },
};

const formatMoney = (value: number) => new Intl.NumberFormat('vi-VN').format(value);

export default function CartComponent({ userId }: { userId: string }) {
  const [items, setItems] = useState<cartService.CartItem[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [formMessage, setFormMessage] = useState<string | null>(null);
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState<CouponInfo | undefined>();
  const [shippingAddress, setShippingAddress] = useState('');
  const [orderMessage, setOrderMessage] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    cartService
      .getCart(userId)
      .then((r) => {
        if (active) {
          setItems(Array.isArray(r.items) ? r.items : []);
          setLoadError(null);
        }
      })
      .catch(() => {
        if (active) {
          setLoadError('API Error');
          setItems([]);
        }
      });
    return () => {
      active = false;
    };
  }, [userId]);

  const shippingFee = items && items.length > 0 ? 50000 : 0;
  const price = useMemo(
    () => calculateOrderPrice(items ?? [], appliedCoupon, shippingFee),
    [appliedCoupon, items, shippingFee],
  );

  if (loadError) {
    return <div data-testid="error-message">{loadError}</div>;
  }

  if (items === null) {
    return <div data-testid="loading-spinner">Loading...</div>;
  }

  if (items.length === 0) {
    return (
      <section className="cart-container">
        {orderMessage && <div data-testid="order-success" className="success">{orderMessage}</div>}
        <div data-testid="empty-cart-message" className="empty-state">Giỏ hàng trống</div>
      </section>
    );
  }

  const handleDelete = (productId: string) => {
    setFormMessage(null);
    cartService.removeFromCart(userId, productId).then((r) => setItems(Array.isArray(r.items) ? r.items : []));
  };

  const handleChange = (productId: string, value: string) => {
    const quantity = parseInt(value || '0', 10);
    if (!Number.isInteger(quantity) || quantity < 1) {
      setFormMessage('Số lượng phải lớn hơn 0');
      return;
    }
    setFormMessage(null);
    cartService.updateQuantity(userId, productId, quantity).then((r) => setItems(Array.isArray(r.items) ? r.items : []));
  };

  const handleApplyCoupon = () => {
    const normalized = couponCode.trim().toUpperCase();
    const coupon = couponCatalog[normalized];
    if (!coupon) {
      setAppliedCoupon(undefined);
      setFormMessage('Mã giảm giá không hợp lệ');
      return;
    }
    setAppliedCoupon(coupon);
    setCouponCode(normalized);
    setFormMessage(`Đã áp dụng mã ${normalized}`);
  };

  const handleCheckout = async () => {
    setOrderMessage(null);
    if (!shippingAddress.trim()) {
      setFormMessage('Vui lòng nhập địa chỉ giao hàng');
      return;
    }

    try {
      const order = await cartService.createOrder({
        userId,
        items,
        couponCode: appliedCoupon?.code,
        shippingFee,
        shippingAddress,
        paymentMethod: 'COD',
      });
      setFormMessage(null);
      setOrderMessage(`Đặt hàng thành công: ${order.orderId}`);
      setItems([]);
    } catch {
      setFormMessage('Không thể đặt hàng');
    }
  };

  return (
    <section className="cart-container">
      <div className="section-heading">
        <p>Checkout</p>
        <h1>Giỏ hàng của bạn</h1>
      </div>

      <div className="cart-layout">
        <div className="cart-list">
          {items.map((it) => (
            <article key={it.productId} className="cart-row">
              <div>
                <h3>{it.productName}</h3>
                <p>{formatMoney(it.price)} VND</p>
              </div>
              <input
                data-testid={`quantity-input-${it.productId}`}
                aria-label={`Số lượng ${it.productName}`}
                type="number"
                min="1"
                value={it.quantity}
                onChange={(e) => handleChange(it.productId, e.target.value)}
              />
              <button data-testid={`delete-product-${it.productId}`} onClick={() => handleDelete(it.productId)}>
                Xóa
              </button>
            </article>
          ))}
        </div>

        <aside className="checkout-panel">
          <div className="summary-line">
            <span>Tạm tính</span>
            <strong data-testid="cart-subtotal">{formatMoney(price.subtotal)}</strong>
          </div>
          <div className="summary-line">
            <span>Giảm giá</span>
            <strong>{formatMoney(price.discount)}</strong>
          </div>
          <div className="summary-line">
            <span>Phí vận chuyển</span>
            <strong data-testid="shipping-fee">{formatMoney(price.shipping)}</strong>
          </div>
          <div className="summary-line total">
            <span>Tổng giỏ hàng</span>
            <strong data-testid="cart-total">{formatMoney(price.subtotal)}</strong>
          </div>
          <div className="summary-line total">
            <span>Cần thanh toán</span>
            <strong data-testid="checkout-total">{formatMoney(price.total)}</strong>
          </div>
          <div data-testid="total-display" className="sr-only">{formatMoney(price.total)}</div>

          <div className="coupon-row">
            <input
              data-testid="coupon-input"
              placeholder="Mã giảm giá"
              value={couponCode}
              onChange={(e) => setCouponCode(e.target.value)}
            />
            <button data-testid="apply-coupon-btn" onClick={handleApplyCoupon}>Áp dụng</button>
          </div>

          <input
            data-testid="shipping-address-input"
            placeholder="Địa chỉ giao hàng"
            value={shippingAddress}
            onChange={(e) => setShippingAddress(e.target.value)}
          />
          <button data-testid="checkout-btn" className="secondary-action">Thanh toán</button>
          <button data-testid="place-order-btn" className="primary-action" onClick={handleCheckout}>Đặt hàng</button>

          {formMessage && <div data-testid="form-message" className="notice inline">{formMessage}</div>}
          {orderMessage && <div data-testid="order-success" className="success">{orderMessage}</div>}
        </aside>
      </div>
    </section>
  );
}
