# 📊 ADVANCED TESTING - Performance & Security

**Version:** 1.0  
**Date:** May 2026  
**Subject:** Performance Testing & Security Testing for ShopCart

---

## 📈 PERFORMANCE TESTING

### Objectives
- Evaluate API response times under load
- Identify performance bottlenecks
- Measure throughput and capacity
- Detect memory leaks and resource issues
- Establish performance baselines

### Test Scenarios

#### **PT_CART_001: Add to Cart - Load Test**

| Property | Value |
|----------|-------|
| **Test ID** | PT_CART_001 |
| **Endpoint** | POST /api/cart/add |
| **Tool** | k6 (Apache JMeter as alternative) |
| **Virtual Users** | 100 concurrent users |
| **Ramp-up Time** | 30 seconds (2-3 users/sec) |
| **Test Duration** | 5 minutes sustained load |
| **Peak Users** | 100 |
| **Think Time** | 2 seconds between requests |

**Expected Results:**
```
- Response Time (P50): ≤ 200ms
- Response Time (P95): ≤ 500ms
- Response Time (P99): ≤ 1000ms
- Throughput: ≥ 50 req/sec
- Error Rate: ≤ 1%
- Success Rate: ≥ 99%
```

**Metrics to Collect:**
- Min/Max/Avg Response Time
- 50th, 95th, 99th percentile response times
- Requests per second (throughput)
- Error count and error rate
- CPU utilization
- Memory usage
- Database query times

---

#### **PT_CART_002: Spike Test**

| Property | Value |
|----------|-------|
| **Virtual Users** | 0 → 500 (sudden spike) |
| **Duration** | 2 minutes spike + 2 minutes sustain |
| **Endpoint** | POST /api/cart/add |

**Expected Results:**
```
- System should handle spike without crashing
- Response time degradation: ≤ 2x under normal load
- Error rate spike: ≤ 5%
- Recovery time: ≤ 30 seconds after spike
```

---

#### **PT_ORDER_001: Create Order - Performance Test**

| Property | Value |
|----------|-------|
| **Test ID** | PT_ORDER_001 |
| **Endpoint** | POST /api/orders |
| **Virtual Users** | 50 concurrent |
| **Duration** | 10 minutes |
| **Think Time** | 5 seconds (checkout is slower) |

**Expected Results:**
```
- Response Time (P50): ≤ 500ms
- Response Time (P95): ≤ 1500ms
- Throughput: ≥ 20 req/sec
- Database transactions: ≤ 200ms
```

---

#### **PT_DB_001: Database Performance**

| Property | Value |
|----------|-------|
| **Query Type** | SELECT, INSERT, UPDATE |
| **Test Focus** | Query execution time |
| **Concurrent Queries** | 100 |

**Benchmarks:**
```
- SELECT queries: ≤ 50ms
- INSERT queries: ≤ 100ms
- UPDATE queries: ≤ 100ms
- Complex joins: ≤ 200ms
```

---

### k6 Performance Test Script

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },   // Ramp up
    { duration: '1m30s', target: 100 }, // Stay at 100
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const baseURL = 'http://localhost:8080';
  
  // Test 1: Add to Cart
  let addCartResponse = http.post(
    `${baseURL}/api/cart/add`,
    JSON.stringify({
      productId: 'P001',
      quantity: 2,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(addCartResponse, {
    'Add to cart status is 200': (r) => r.status === 200,
    'Response time < 500ms': (r) => r.timings.duration < 500,
    'Response contains success': (r) => r.body.includes('success'),
  });

  sleep(2);

  // Test 2: Create Order
  let orderResponse = http.post(
    `${baseURL}/api/orders`,
    JSON.stringify({
      userId: 'user01',
      items: [
        { productId: 'P001', quantity: 2, price: 15000000 },
        { productId: 'P002', quantity: 1, price: 500000 },
      ],
      couponCode: 'SALE10',
      shippingFee: 50000,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(orderResponse, {
    'Create order status is 201': (r) => r.status === 201,
    'Response time < 1000ms': (r) => r.timings.duration < 1000,
    'Order created successfully': (r) => r.body.includes('orderId'),
  });

  sleep(5);
}
```

---

## 🔒 SECURITY TESTING

### Objectives
- Identify security vulnerabilities
- Test authentication and authorization
- Validate input sanitization
- Prevent SQL injection, XSS, IDOR
- Test CORS and headers

### Test Cases

#### **ST_SQLI_001: SQL Injection - Cart Endpoint**

| Property | Value |
|----------|-------|
| **Test ID** | ST_SQLI_001 |
| **Endpoint** | POST /api/cart/add |
| **Vulnerability Type** | SQL Injection |
| **Priority** | 🔴 Critical |

**Attack Payload:**
```
productId: "P001' OR '1'='1"
productId: "P001'; DROP TABLE products; --"
productId: "P001' UNION SELECT * FROM users --"
```

**Expected Result:**
- ❌ Attack should FAIL
- ✅ Request should be rejected or sanitized
- ✅ No database modification should occur
- ✅ Error message should not reveal DB details

**Validation:**
```
- Input validation: Only alphanumeric + underscore
- Parameterized queries: Use prepared statements
- Output encoding: Proper escaping
```

---

#### **ST_XSS_001: Cross-Site Scripting - Cart Display**

| Property | Value |
|----------|-------|
| **Test ID** | ST_XSS_001 |
| **Vulnerability Type** | Stored/Reflected XSS |
| **Priority** | 🔴 Critical |

**Attack Payload:**
```javascript
// Stored XSS via product name
productName: "<img src=x onerror='alert(1)'>"

// Reflected XSS via search
search: "<script>alert('XSS')</script>"

// HTML injection
productName: "<iframe src='evil.com'></iframe>"
```

**Expected Result:**
- ❌ Script should NOT execute
- ✅ Content should be HTML-encoded
- ✅ Output: `&lt;img src=x onerror=...&gt;`
- ✅ Browser console: No errors or warnings

**Validation:**
```
- HTML encode all user input
- Sanitize product names, descriptions
- Use CSP headers
- Validate content-type responses
```

---

#### **ST_IDOR_001: Insecure Direct Object Reference - Order**

| Property | Value |
|----------|-------|
| **Test ID** | ST_IDOR_001 |
| **Endpoint** | GET /api/orders/{orderId} |
| **Vulnerability Type** | IDOR (Broken Access Control) |
| **Priority** | 🔴 Critical |

**Attack Scenario:**
```
1. User A (user01) creates order ORD-001
2. User B (user02) tries to access: GET /api/orders/ORD-001
3. System should reject access
```

**Test Cases:**
```
Case 1: Access other user's order
- Request: GET /api/orders/ORD-20260509-USER02
- Auth: Bearer token-user01
- Expected: 403 Forbidden

Case 2: Access non-existent order
- Request: GET /api/orders/INVALID-123
- Expected: 404 Not Found

Case 3: No authorization header
- Request: GET /api/orders/ORD-001 (no auth)
- Expected: 401 Unauthorized
```

**Validation:**
```
- Verify user ownership before returning order
- Check authorization on every endpoint
- Log unauthorized access attempts
```

---

#### **ST_CSRF_001: Cross-Site Request Forgery - Order**

| Property | Value |
|----------|-------|
| **Test ID** | ST_CSRF_001 |
| **Endpoint** | POST /api/orders |
| **Vulnerability Type** | CSRF |
| **Priority** | 🟠 High |

**Attack Scenario:**
```
1. Attacker creates malicious website
2. User is logged into ShopCart
3. User visits attacker's site
4. Site makes POST /api/orders without user's consent
```

**Expected Defense:**
```
- CSRF token required in POST/PUT/DELETE
- Token validation: Compare request token with session token
- SameSite cookie attribute: Strict or Lax
- Referer header validation
```

**Test:**
```
POST /api/orders
Content-Type: application/json
Cookie: JSESSIONID=...
X-CSRF-Token: (missing or invalid)

Expected: 403 Forbidden
```

---

#### **ST_AUTH_001: Authentication - Missing Token**

| Property | Value |
|----------|-------|
| **Test ID** | ST_AUTH_001 |
| **Endpoint** | POST /api/cart/add |
| **Priority** | 🔴 Critical |

**Test Cases:**
```
1. No Authorization header
   GET /api/cart
   Expected: 401 Unauthorized

2. Invalid token format
   Authorization: Bearer invalid-token-xyz
   Expected: 401 Unauthorized

3. Expired token
   Authorization: Bearer <expired-jwt>
   Expected: 401 Unauthorized

4. Token for different user
   Authorization: Bearer <token-user02> (request as user01)
   Expected: 403 Forbidden or 401
```

---

#### **ST_VALID_001: Input Validation - Quantity**

| Property | Value |
|----------|-------|
| **Test ID** | ST_VALID_001 |
| **Parameter** | Quantity in cart add |
| **Priority** | 🟠 High |

**Test Cases:**
```
1. Negative quantity
   quantity: -10
   Expected: 400 Bad Request

2. Zero quantity
   quantity: 0
   Expected: 400 Bad Request

3. Non-integer
   quantity: "abc"
   Expected: 400 Bad Request

4. Float
   quantity: 2.5
   Expected: 400 Bad Request or rounded

5. Null/Empty
   quantity: null
   Expected: 400 Bad Request

6. SQL injection
   quantity: "1 OR 1=1"
   Expected: 400 Bad Request
```

---

#### **ST_HEADER_001: Security Headers Validation**

| Property | Value |
|----------|-------|
| **Test ID** | ST_HEADER_001 |
| **Priority** | 🟠 High |

**Expected Security Headers:**
```
✅ Content-Security-Policy: default-src 'self'
✅ X-Content-Type-Options: nosniff
✅ X-Frame-Options: DENY
✅ X-XSS-Protection: 1; mode=block
✅ Strict-Transport-Security: max-age=31536000
✅ Access-Control-Allow-Origin: (specific domain, not *)
✅ Authorization: Required for protected endpoints
```

**Test Script:**
```bash
curl -v http://localhost:8080/api/cart

# Verify headers in response
# Should see all security headers
```

---

#### **ST_CORS_001: CORS Configuration**

| Property | Value |
|----------|-------|
| **Test ID** | ST_CORS_001 |
| **Priority** | 🟠 High |

**Test Cases:**
```
1. Valid origin (localhost:3000)
   Origin: http://localhost:3000
   Expected: CORS headers present

2. Invalid origin (attacker.com)
   Origin: http://attacker.com
   Expected: No CORS headers or 403

3. Credentials with wildcard
   Allow-Origin: *
   Credentials: include
   Expected: SHOULD FAIL (security risk)
```

---

### Security Testing Checklist

```
Authentication & Authorization
  ❌ Missing authentication token
  ❌ Invalid token format
  ❌ Expired token
  ❌ Access other user's resources (IDOR)
  ❌ Privilege escalation
  
Input Validation
  ❌ SQL Injection (special chars, quotes, comments)
  ❌ XSS payloads (script tags, event handlers)
  ❌ HTML injection
  ❌ Command injection
  ❌ Path traversal
  ❌ Null bytes
  
Data Protection
  ❌ Sensitive data in logs
  ❌ Unencrypted data transmission
  ❌ Password exposure
  ❌ Credit card validation
  
API Security
  ❌ Rate limiting
  ❌ DDoS protection
  ❌ CSRF tokens
  ❌ CORS configuration
  ❌ Security headers
  
Session Management
  ❌ Session fixation
  ❌ Session hijacking
  ❌ Cookie attributes (HttpOnly, Secure, SameSite)
```

---

## 📊 Testing Results Template

### Performance Testing Results

```
Test: PT_CART_001 - Add to Cart Load Test
Date: 09/05/2026
Duration: 5 minutes
Virtual Users: 100

Results:
- Min Response Time: 45ms
- Max Response Time: 2500ms
- Avg Response Time: 250ms
- P50 (Median): 180ms
- P95: 450ms
- P99: 1200ms
- Throughput: 85 req/sec
- Success Rate: 99.8%
- Error Rate: 0.2%

CPU: 65%
Memory: 512MB / 2GB
Database Queries: 80-120ms

Status: ✅ PASSED
- All metrics within acceptable ranges
- No memory leaks detected
- Database performance adequate
```

### Security Testing Results

```
Test: ST_SQLI_001 - SQL Injection
Date: 09/05/2026

Payload 1: P001' OR '1'='1
Result: ✅ REJECTED - Input validation working
Error: "Invalid product ID format"

Payload 2: P001'; DROP TABLE products; --
Result: ✅ REJECTED - Parameterized queries in use
No data modified

Payload 3: UNION SELECT * FROM users
Result: ✅ REJECTED - Prepared statements prevent injection

Status: ✅ PASSED
- All SQL injection attempts blocked
- Input properly validated
- Parameterized queries implemented
```

---

## 🔍 Recommendations

### Performance
1. Implement database query optimization
2. Add caching layer (Redis) for frequently accessed data
3. Implement pagination for list endpoints
4. Monitor slow queries in production
5. Set up alerting for response time degradation

### Security
1. Implement rate limiting (prevent brute force)
2. Add Web Application Firewall (WAF)
3. Implement security logging and monitoring
4. Regular security updates for dependencies
5. Penetration testing by security professionals
6. OWASP Top 10 compliance review
7. Security headers hardening
8. Data encryption at rest and in transit

---

**Document Version:** 1.0  
**Status:** ✅ Complete  
**Next Review:** After implementation
