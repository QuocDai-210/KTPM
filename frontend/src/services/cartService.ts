import axios from 'axios';

export async function getCart(userId: string) {
  const resp = await axios.get(`/api/cart/${userId}`);
  return resp.data;
}

export async function removeFromCart(userId: string, productId: string) {
  const resp = await axios.delete(`/api/cart/${userId}/${productId}`);
  return resp.data;
}

export async function updateQuantity(userId: string, productId: string, quantity: number) {
  const resp = await axios.put(`/api/cart/update`, { userId, productId, quantity });
  return resp.data;
}
