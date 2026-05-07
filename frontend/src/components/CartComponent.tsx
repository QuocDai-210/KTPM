import React, { useEffect, useState } from 'react';
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

  useEffect(() => {
    setItems(null);
    cartService
      .getCart(userId)
      .then((r) => {
        setItems(r.items || []);
      })
      .catch(() => setError('API Error'));
  }, [userId]);

  if (error) {
    return <div data-testid="error-message">{error}</div>;
  }

  if (items === null) {
    return <div data-testid="loading-spinner">Loading...</div>;
  }

  if (items.length === 0) {
    return <div data-testid="empty-cart-message">Giỏ hàng trống</div>;
  }

  const total = items.reduce((s, it) => s + it.price * it.quantity, 0);

  const handleDelete = (productId: string) => {
    cartService.removeFromCart(userId, productId).then((r) => setItems(r.items || []));
  };

  const handleChange = (productId: string, value: string) => {
    const q = parseInt(value || '0', 10);
    if (Number.isNaN(q)) return;
    cartService.updateQuantity(userId, productId, q).then((r) => setItems(r.items || []));
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
      <div data-testid="cart-total">{new Intl.NumberFormat('vi-VN').format(total)}</div>
    </div>
  );
}
