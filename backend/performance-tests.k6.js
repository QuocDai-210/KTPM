import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    cart_add_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },
        { duration: '4m', target: 100 },
        { duration: '30s', target: 0 },
      ],
      exec: 'cartAddScenario',
    },
    order_create_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },
        { duration: '4m', target: 50 },
        { duration: '30s', target: 0 },
      ],
      exec: 'orderCreateScenario',
      startTime: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const commonHeaders = {
  headers: {
    'Content-Type': 'application/json',
    Authorization: 'Bearer perf-token',
  },
};

export function cartAddScenario() {
  const payload = JSON.stringify({
    productId: 'P001',
    quantity: 1,
  });

  const res = http.post(`${BASE_URL}/api/cart/add`, payload, commonHeaders);
  check(res, {
    'cart add status is 200': (r) => r.status === 200,
    'cart add latency < 800ms': (r) => r.timings.duration < 800,
  });
  sleep(1);
}

export function orderCreateScenario() {
  const payload = JSON.stringify({
    userId: 'user01',
    items: [
      {
        productId: 'P001',
        quantity: 1,
        price: 1000000,
      },
    ],
    shippingFee: 50000,
    shippingAddress: '123 Perf St',
    paymentMethod: 'COD',
  });

  const res = http.post(`${BASE_URL}/api/orders`, payload, commonHeaders);
  check(res, {
    'order create status is 201': (r) => r.status === 201,
    'order create latency < 1000ms': (r) => r.timings.duration < 1000,
  });
  sleep(1);
}
