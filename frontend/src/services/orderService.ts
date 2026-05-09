import axios from 'axios';

const authHeaders = {
  Authorization: 'Bearer token123',
};

export interface OrderItem {
  productId: string;
  quantity: number;
  price: number;
}

export interface OrderRequest {
  userId?: string;
  items: OrderItem[];
  totalPrice?: number;
  couponCode?: string;
  shippingFee?: number;
  shippingAddress?: string;
  paymentMethod?: string;
}

export interface OrderResponse {
  orderId: string;
  status: string;
  totalPrice: number;
  message?: string;
}

export interface PriceResponse {
  subtotal: number;
  discount: number;
  shipping: number;
  total: number;
}

export async function createOrder(payload: OrderRequest) {
  const resp = await axios.post('/api/orders', payload, { headers: authHeaders });
  return resp.data as OrderResponse;
}

export async function applyCoupon(code: string) {
  const resp = await axios.post('/api/orders/coupon', { code }, { headers: authHeaders });
  return resp.data as PriceResponse;
}

export async function calculatePrice(items: OrderItem[]) {
  const resp = await axios.post('/api/orders/price', { items }, { headers: authHeaders });
  return resp.data as PriceResponse;
}
