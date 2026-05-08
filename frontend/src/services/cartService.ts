import axios from 'axios';

const authHeaders = {
  Authorization: 'Bearer token123',
};

export interface Product {
  id: string;
  name: string;
  price: number;
  stock: number;
}

export interface CartItem {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}

export async function getProducts() {
  const resp = await axios.get('/api/products', { headers: authHeaders });
  return resp.data as Product[];
}

export async function addToCart(userId: string, productId: string, quantity: number) {
  const resp = await axios.post(
    '/api/cart/add',
    { userId, productId, quantity },
    { headers: authHeaders },
  );
  return resp.data;
}

export async function getCart(userId: string) {
  const resp = await axios.get(`/api/cart/${userId}`, { headers: authHeaders });
  return resp.data;
}

export async function removeFromCart(userId: string, productId: string) {
  const resp = await axios.delete(`/api/cart/${userId}/${productId}`, { headers: authHeaders });
  return resp.data;
}

export async function updateQuantity(userId: string, productId: string, quantity: number) {
  const resp = await axios.put(`/api/cart/update`, { userId, productId, quantity }, { headers: authHeaders });
  return resp.data;
}

export async function createOrder(payload: {
  userId: string;
  items: Array<{ productId: string; quantity: number; price: number }>;
  couponCode?: string;
  shippingFee?: number;
  shippingAddress?: string;
  paymentMethod?: string;
}) {
  const resp = await axios.post('/api/orders', payload, { headers: authHeaders });
  return resp.data;
}
