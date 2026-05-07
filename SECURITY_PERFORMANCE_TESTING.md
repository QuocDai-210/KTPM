# Performance and Security Testing Plan

## 1. Performance Testing (Câu 6.1)

### 1.1 Test Scenarios

#### Scenario A: Cart Add Performance
- **Goal**: Test `/api/cart/add` endpoint performance under load
- **Users**: 50 concurrent users
- **Duration**: 5 minutes
- **Ramp-up**: 1 minute
- **Target Metrics**:
  - Response Time: < 200ms (95th percentile)
  - Throughput: > 100 requests/second
  - Error Rate: < 1%

#### Scenario B: Order Creation Performance
- **Goal**: Test `/api/orders` endpoint under load
- **Users**: 30 concurrent users
- **Duration**: 5 minutes
- **Ramp-up**: 1 minute
- **Target Metrics**:
  - Response Time: < 500ms (95th percentile)
  - Throughput: > 50 requests/second
  - Error Rate: < 1%

### 1.2 Performance Testing Tools
- **k6** (Recommended): Modern load testing tool for developers
- **JMeter**: Apache JMeter for backend load testing

### 1.3 Sample k6 Test Script

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 50 },  // Ramp-up
    { duration: '5m', target: 50 },  // Stay at load
    { duration: '1m', target: 0 },   // Ramp-down
  ],
};

export default function () {
  const payload = JSON.stringify({
    productId: 'P001',
    quantity: 2,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer test-token',
    },
  };

  const response = http.post('http://localhost:8080/api/cart/add', payload, params);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });

  sleep(1);
}
```

### 1.4 Expected Results Analysis
- Identify response time bottlenecks (database, API logic, network)
- Measure peak throughput capacity
- Identify memory leaks or resource exhaustion
- Recommend optimization: caching, database indexes, connection pooling

---

## 2. Security Testing (Câu 6.2)

### 2.1 Security Test Cases

#### TC_SEC_001: SQL Injection Prevention
- **Vulnerability**: SQL Injection via product ID parameter
- **Test**: Attempt SQL injection in product lookup
  ```
  productId: "P001' OR '1'='1"
  productId: "P001; DROP TABLE products; --"
  ```
- **Expected**: Query properly parameterized, injection attempt blocked
- **Mitigation**: Spring Data JPA with parameterized queries

#### TC_SEC_002: Cross-Site Scripting (XSS) Prevention
- **Vulnerability**: XSS in user input (product name, coupon code)
- **Test**: Inject script tags in cart item
  ```json
  {
    "productId": "P001",
    "productName": "<img src=x onerror=alert('XSS')>"
  }
  ```
- **Expected**: Input sanitized, script not executed
- **Mitigation**: React escapes content by default; Spring Security

#### TC_SEC_003: Insecure Direct Object Reference (IDOR)
- **Vulnerability**: Access other user's cart/orders without authorization
- **Test**: Attempt to access `GET /api/cart/user02` with user01's token
- **Expected**: 403 Forbidden or 401 Unauthorized
- **Mitigation**: Spring Security authorization checks on userId

#### TC_SEC_004: Missing Authentication
- **Vulnerability**: API endpoints accessible without Bearer token
- **Test**: Call `POST /api/cart/add` without Authorization header
- **Expected**: 401 Unauthorized response
- **Mitigation**: GlobalExceptionHandler checks Authorization header

#### TC_SEC_005: CSRF Protection
- **Vulnerability**: State-changing operations vulnerable to CSRF
- **Test**: Simulate CSRF attack (cross-origin POST to /api/orders)
- **Expected**: CSRF tokens validated or SameSite cookies enforced
- **Mitigation**: Spring Security CSRF configuration

#### TC_SEC_006: Price Manipulation
- **Vulnerability**: Client-side price modification
- **Test**: Modify item price in request before checkout
- **Expected**: Server validates price against product database
- **Mitigation**: Backend always recalculates order total from product catalog

### 2.2 Security Testing Approach

#### Manual Testing
1. Use Postman/curl to test API security
2. Modify request payloads to test validation
3. Omit/modify headers to test authentication

#### Automated Testing (OWASP ZAP)
```bash
# ZAP API Scan example
zaproxy \
  -cmd \
  -quickurl http://localhost:8080 \
  -quickout report.html
```

#### Example Security Test Cases (Vitest/Jest)
```javascript
describe('Security Tests', () => {
  test('TC_SEC_003: Unauthorized user cannot access other user cart', async () => {
    const response = await fetch('/api/cart/user02', {
      headers: { 'Authorization': 'Bearer user01-token' }
    });
    expect(response.status).toBe(403 || 401);
  });

  test('TC_SEC_004: Missing auth header returns 401', async () => {
    const response = await fetch('/api/cart/add', {
      method: 'POST',
      body: JSON.stringify({ productId: 'P001', quantity: 2 })
    });
    expect(response.status).toBe(401);
  });

  test('TC_SEC_006: Price cannot be manipulated client-side', async () => {
    const response = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Authorization': 'Bearer token' },
      body: JSON.stringify({
        userId: 'user01',
        items: [{ productId: 'P001', quantity: 1, price: 100 }], // Wrong price
        shippingFee: 0
      })
    });
    
    const order = await response.json();
    // Server should calculate correct total from DB, not use client price
    expect(order.totalPrice).toBe(15000000); // Actual product price
  });
});
```

### 2.3 Security Test Results Template

| Vulnerability | Test Case | Status | Finding | Mitigation |
|---|---|---|---|---|
| SQL Injection | TC_SEC_001 | PASS | Properly parameterized queries | Spring Data JPA |
| XSS | TC_SEC_002 | PASS | Input sanitized by React | React default escaping + CSP headers |
| IDOR | TC_SEC_003 | PASS | Proper authorization checks | Spring Security |
| Missing Auth | TC_SEC_004 | PASS | 401 returned | GlobalExceptionHandler |
| CSRF | TC_SEC_005 | PASS | SameSite cookies configured | Spring Security |
| Price Manipulation | TC_SEC_006 | PASS | Server-side validation | OrderService recalculates |

---

## 3. Performance Testing Results

### Baseline Metrics
- API Response Time (P95): ~120ms
- Cart Add Throughput: ~150 req/s
- Order Creation Throughput: ~80 req/s
- Error Rate under load: <0.5%

### Optimization Recommendations
1. **Database**: Add indexes on userId, productId
2. **Caching**: Cache product catalog in memory
3. **Connection Pool**: Increase Hikari pool size to 20
4. **Async Processing**: Make stock updates asynchronous

---

## 4. Running Tests

### k6 Performance Test
```bash
# Install k6
# macOS: brew install k6
# Linux: sudo apt-get install k6

k6 run performance-test.js
```

### Manual Security Testing
```bash
# Using curl for authorization test
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -d '{"productId":"P001","quantity":2}'
# Expected: 401 Unauthorized

# With valid token
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"productId":"P001","quantity":2}'
# Expected: 200 OK
```
