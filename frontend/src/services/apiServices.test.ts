import { beforeEach, describe, expect, test, vi } from 'vitest';
import axios from 'axios';
import * as cartService from './cartService';
import * as inventoryService from './inventoryService';
import * as orderService from './orderService';

vi.mock('axios');

const mockedAxios = vi.mocked(axios);
const headers = { headers: { Authorization: 'Bearer token123' } };

describe('API services', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('cartService gọi đúng products, cart và mutation APIs', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({ data: [{ id: 'P001', name: 'Laptop', price: 1, stock: 2 }] })
      .mockResolvedValueOnce({ data: { items: [] } });
    mockedAxios.post
      .mockResolvedValueOnce({ data: { success: true } })
      .mockResolvedValueOnce({ data: { orderId: 'ORD-001' } });
    mockedAxios.delete.mockResolvedValueOnce({ data: { items: [] } });
    mockedAxios.put.mockResolvedValueOnce({ data: { itemCount: 3 } });

    await expect(cartService.getProducts()).resolves.toEqual([
      { id: 'P001', name: 'Laptop', price: 1, stock: 2 },
    ]);
    await expect(cartService.addToCart('user01', 'P001', 2)).resolves.toEqual({ success: true });
    await expect(cartService.getCart('user01')).resolves.toEqual({ items: [] });
    await expect(cartService.removeFromCart('user01', 'P001')).resolves.toEqual({ items: [] });
    await expect(cartService.updateQuantity('user01', 'P001', 3)).resolves.toEqual({ itemCount: 3 });
    await expect(cartService.createOrder({
      userId: 'user01',
      items: [{ productId: 'P001', quantity: 1, price: 15000000 }],
    })).resolves.toEqual({ orderId: 'ORD-001' });

    expect(mockedAxios.get).toHaveBeenNthCalledWith(1, '/api/products', headers);
    expect(mockedAxios.post).toHaveBeenNthCalledWith(
      1,
      '/api/cart/add',
      { userId: 'user01', productId: 'P001', quantity: 2 },
      headers,
    );
    expect(mockedAxios.get).toHaveBeenNthCalledWith(2, '/api/cart/user01', headers);
    expect(mockedAxios.delete).toHaveBeenCalledWith('/api/cart/user01/P001', headers);
    expect(mockedAxios.put).toHaveBeenCalledWith(
      '/api/cart/update',
      { userId: 'user01', productId: 'P001', quantity: 3 },
      headers,
    );
    expect(mockedAxios.post).toHaveBeenNthCalledWith(
      2,
      '/api/orders',
      { userId: 'user01', items: [{ productId: 'P001', quantity: 1, price: 15000000 }] },
      headers,
    );
  });

  test('orderService gọi đúng order, coupon và price APIs', async () => {
    mockedAxios.post
      .mockResolvedValueOnce({ data: { orderId: 'ORD-001', status: 'PENDING', totalPrice: 1 } })
      .mockResolvedValueOnce({ data: { subtotal: 10, discount: 1, shipping: 2, total: 11 } })
      .mockResolvedValueOnce({ data: { subtotal: 20, discount: 0, shipping: 2, total: 22 } });

    const payload: orderService.OrderRequest = {
      userId: 'user01',
      items: [{ productId: 'P001', quantity: 1, price: 15000000 }],
      paymentMethod: 'COD',
    };
    await expect(orderService.createOrder(payload)).resolves.toEqual({
      orderId: 'ORD-001',
      status: 'PENDING',
      totalPrice: 1,
    });
    await expect(orderService.applyCoupon('SALE10')).resolves.toEqual({
      subtotal: 10,
      discount: 1,
      shipping: 2,
      total: 11,
    });
    await expect(orderService.calculatePrice(payload.items)).resolves.toEqual({
      subtotal: 20,
      discount: 0,
      shipping: 2,
      total: 22,
    });

    expect(mockedAxios.post).toHaveBeenNthCalledWith(1, '/api/orders', payload, headers);
    expect(mockedAxios.post).toHaveBeenNthCalledWith(2, '/api/orders/coupon', { code: 'SALE10' }, headers);
    expect(mockedAxios.post).toHaveBeenNthCalledWith(3, '/api/orders/price', { items: payload.items }, headers);
  });

  test('inventoryService gọi đúng API kiểm tra tồn kho', async () => {
    const items = [{ productId: 'P001', quantity: 2 }];
    mockedAxios.post.mockResolvedValueOnce({ data: { available: true } });

    await expect(inventoryService.checkStock(items)).resolves.toEqual({ available: true });

    expect(mockedAxios.post).toHaveBeenCalledWith('/api/inventory/check', { items }, headers);
  });
});
