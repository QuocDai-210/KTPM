import { useEffect, useMemo, useState } from 'react';
import * as cartService from '../services/cartService';
import { calculateOrderPrice, type CouponInfo } from '../utils/priceCalculation';

const couponCatalog: Record<string, CouponInfo> = {
  SALE10: { code: 'SALE10', discountType: 'PERCENT', discountValue: 10 },
  SALE20: { code: 'SALE20', discountType: 'PERCENT', discountValue: 20 },
  FIXED100K: { code: 'FIXED100K', discountType: 'FIXED', discountValue: 100000 },
};

const formatMoney = (value: number) => new Intl.NumberFormat('vi-VN').format(value);

type CartComponentProps = {
  userId: string;
  onCartCountChange?: (count: number) => void;
};

export default function CartComponent({ userId, onCartCountChange }: CartComponentProps) {
  const [items, setItems] = useState<cartService.CartItem[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [formMessage, setFormMessage] = useState<string | null>(null);
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState<CouponInfo | undefined>();
  const [shippingAddress, setShippingAddress] = useState('');
  const [orderMessage, setOrderMessage] = useState<string | null>(null);

  const syncItems = (nextItems: cartService.CartItem[]) => {
    setItems(nextItems);
    onCartCountChange?.(nextItems.reduce((sum, item) => sum + item.quantity, 0));
  };

  useEffect(() => {
    let active = true;
    cartService
      .getCart(userId)
      .then((response) => {
        if (!active) {
          return;
        }
        const nextItems = Array.isArray(response.items) ? response.items : [];
        syncItems(nextItems);
        setLoadError(null);
      })
      .catch(() => {
        if (!active) {
          return;
        }
        setLoadError('API Error');
        syncItems([]);
      });

    return () => {
      active = false;
    };
  }, [onCartCountChange, userId]);

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
        {orderMessage && (
          <div data-testid="order-success" className="success">
            {orderMessage}
          </div>
        )}
        <div data-testid="empty-cart-message" className="empty-state">
          Gio hang trong
        </div>
      </section>
    );
  }

  const handleDelete = (productId: string) => {
    setFormMessage(null);
    cartService
      .removeFromCart(userId, productId)
      .then((response) => syncItems(Array.isArray(response.items) ? response.items : []));
  };

  const handleChange = (productId: string, value: string) => {
    const quantity = parseInt(value || '0', 10);
    if (!Number.isInteger(quantity) || quantity < 1) {
      setFormMessage('So luong phai lon hon 0');
      return;
    }
    setFormMessage(null);
    cartService
      .updateQuantity(userId, productId, quantity)
      .then((response) => syncItems(Array.isArray(response.items) ? response.items : []));
  };

  const handleApplyCoupon = () => {
    const normalized = couponCode.trim().toUpperCase();
    const coupon = couponCatalog[normalized];
    if (!coupon) {
      setAppliedCoupon(undefined);
      setFormMessage('Ma giam gia khong hop le');
      return;
    }
    setAppliedCoupon(coupon);
    setCouponCode(normalized);
    setFormMessage(`Da ap dung ma ${normalized}`);
  };

  const handleCheckout = async () => {
    setOrderMessage(null);
    if (!shippingAddress.trim()) {
      setFormMessage('Vui long nhap dia chi giao hang');
      return;
    }

    try {
      const checkoutItems = [...items];
      const order = await cartService.createOrder({
        userId,
        items: checkoutItems,
        couponCode: appliedCoupon?.code,
        shippingFee,
        shippingAddress,
        paymentMethod: 'COD',
      });

      await Promise.all(
        checkoutItems.map((item) => cartService.removeFromCart(userId, item.productId)),
      );

      setFormMessage(null);
      setOrderMessage(`Dat hang thanh cong: ${order.orderId}`);
      setAppliedCoupon(undefined);
      setCouponCode('');
      setShippingAddress('');
      syncItems([]);
    } catch {
      setFormMessage('Khong the dat hang');
    }
  };

  return (
    <section className="cart-container">
      <div className="section-heading">
        <p>Checkout</p>
        <h1>Gio hang cua ban</h1>
      </div>

      <div className="cart-layout">
        <div className="cart-list">
          {items.map((item) => (
            <article key={item.productId} className="cart-row">
              <div>
                <h3>{item.productName}</h3>
                <p>{formatMoney(item.price)} VND</p>
              </div>
              <input
                data-testid={`quantity-input-${item.productId}`}
                aria-label={`So luong ${item.productName}`}
                type="number"
                min="1"
                value={item.quantity}
                onChange={(event) => handleChange(item.productId, event.target.value)}
              />
              <button
                data-testid={`delete-product-${item.productId}`}
                onClick={() => handleDelete(item.productId)}
              >
                Xoa
              </button>
            </article>
          ))}
        </div>

        <aside className="checkout-panel">
          <div className="summary-line">
            <span>Tam tinh</span>
            <strong data-testid="cart-subtotal">{formatMoney(price.subtotal)}</strong>
          </div>
          <div className="summary-line">
            <span>Giam gia</span>
            <strong>{formatMoney(price.discount)}</strong>
          </div>
          <div className="summary-line">
            <span>Phi van chuyen</span>
            <strong data-testid="shipping-fee">{formatMoney(price.shipping)}</strong>
          </div>
          <div className="summary-line total">
            <span>Tong gio hang</span>
            <strong data-testid="cart-total">{formatMoney(price.subtotal)}</strong>
          </div>
          <div className="summary-line total">
            <span>Can thanh toan</span>
            <strong data-testid="checkout-total">{formatMoney(price.total)}</strong>
          </div>
          <div data-testid="total-display" className="sr-only">
            {formatMoney(price.total)}
          </div>

          <div className="coupon-row">
            <input
              data-testid="coupon-input"
              placeholder="Ma giam gia"
              value={couponCode}
              onChange={(event) => setCouponCode(event.target.value)}
            />
            <button data-testid="apply-coupon-btn" onClick={handleApplyCoupon}>
              Ap dung
            </button>
          </div>

          <input
            data-testid="shipping-address-input"
            placeholder="Dia chi giao hang"
            value={shippingAddress}
            onChange={(event) => setShippingAddress(event.target.value)}
          />
          <button data-testid="checkout-btn" className="secondary-action">
            Thanh toan
          </button>
          <button
            data-testid="place-order-btn"
            className="primary-action"
            onClick={handleCheckout}
          >
            Dat hang
          </button>

          {formMessage && (
            <div data-testid="form-message" className="notice inline">
              {formMessage}
            </div>
          )}
          {orderMessage && (
            <div data-testid="order-success" className="success">
              {orderMessage}
            </div>
          )}
        </aside>
      </div>
    </section>
  );
}
