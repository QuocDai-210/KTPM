import axios from 'axios';

const authHeaders = {
  Authorization: 'Bearer token123',
};

export interface StockCheckRequestItem {
  productId: string;
  quantity: number;
}

export interface StockCheckResponse {
  available: boolean;
  message?: string;
}

export async function checkStock(items: StockCheckRequestItem[]) {
  const resp = await axios.post('/api/inventory/check', { items }, { headers: authHeaders });
  return resp.data as StockCheckResponse;
}
