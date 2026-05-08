# 🧪 Testing Guide - ShopCart Project

**Version:** 1.1  
**Date:** 09/05/2026  
**Status:** ✅ Complete

---

## 📋 Project Overview

ShopCart is a comprehensive e-commerce testing project covering:
- **Frontend:** React 19.x + TypeScript with Vitest & Playwright
- **Backend:** Spring Boot 4.0.6 + Java 21 with JUnit 5 & Mockito
- **Testing:** Unit, Integration, Mock, E2E, Performance, & Security tests

---

## 🏗️ Project Structure

```
ShopCart_FE_BE/
├── frontend/                           # React application
│   ├── src/
│   │   ├── components/
│   │   │   ├── Cart.integration.test.tsx      ✅ Component integration
│   │   │   ├── Cart.mock.test.tsx             ✅ Mock tests
│   │   │   └── Purchase.mock.test.tsx         ✅ Mock tests
│   │   ├── utils/
│   │   │   ├── cartValidation.test.ts         ✅ Unit tests
│   │   │   └── priceCalculation.test.ts       ✅ Unit tests
│   │   └── test/setup.ts                      ✅ Vitest setup
│   ├── e2e/
│   │   ├── cart.e2e.spec.ts                   ✅ E2E tests
│   │   ├── purchase.e2e.spec.ts               ✅ E2E tests
│   │   ├── checkout.e2e.spec.ts               ✅ Enhanced E2E
│   │   └── pages/CheckoutPage.ts              ✅ Page Object Model
│   ├── vitest.config.ts                       ✅ Config
│   ├── playwright.config.ts                   ✅ Updated with Firefox/WebKit
│   └── package.json                           ✅ Test scripts
│
├── backend/                            # Spring Boot application
│   └── src/test/java/com/shopcart/
│       ├── service/
│       │   ├── CartServiceTest.java            ✅ 14 unit tests
│       │   ├── CartServiceMockTest.java        ✅ 7 mock tests
│       │   ├── OrderServiceTest.java           ✅ 14 unit tests
│       │   └── OrderServiceMockTest.java       ✅ 10 mock tests
│       └── controller/
│           ├── CartControllerIntegrationTest.java   ✅ Integration
│           ├── CartControllerMockTest.java          ✅ Mock tests
│           └── OrderControllerIntegrationTest.java  ✅ Integration
│
├── docs/
│   ├── TESTCASES.md                   ✅ 10 detailed test cases
│   └── ADVANCED_TESTING.md            ✅ Performance & Security
│
├── .github/workflows/
│   └── ci.yml                         ✅ CI/CD pipeline
│
└── ASSESSMENT_REPORT.md               ✅ Evaluation report
```

---

## 🚀 Quick Start

### Prerequisites

**Frontend:**
```bash
Node.js 22+
npm or yarn
```

**Backend:**
```bash
Java 21
Maven 3.8+
```

### Installation

```bash
# Frontend
cd frontend
npm install

# Backend
cd backend
./mvnw clean install
```

---

## 🧪 Running Tests

### Frontend Tests

#### Unit Tests (Vitest)

```bash
cd frontend

# Run all unit tests
npm test

# Run with coverage
npm test -- --coverage

# Run specific test file
npm test cartValidation.test.ts

# Watch mode
npm test -- --watch
```

**Expected Output:**
```
✓ cartValidation.test.ts (7)
✓ priceCalculation.test.ts (5)
✓ Cart.integration.test.tsx (1)
✓ Cart.mock.test.tsx (5)
✓ Purchase.mock.test.tsx (6)

Tests:  24 passed (24)
Time:   2.5s
Coverage: 95% statements, 92% branches, 90% functions
```

#### E2E Tests (Playwright)

```bash
cd frontend

# Run all E2E tests
npm run test:e2e

# Run specific test file
npm run test:e2e cart.e2e.spec.ts

# Run with UI mode
npm run test:e2e:ui

# Run with debug mode
npm run test:e2e:debug

# Generate HTML report
npm run test:e2e:report
```

**Supported Browsers:**
- ✅ Chromium
- ✅ Firefox (updated)
- ✅ WebKit (updated)

**Expected Output:**
```
✓ Cart E2E Tests (3)
✓ Checkout E2E Tests (10)
✓ Checkout E2E - Edge Cases (2)

Tests:  15 passed (15)
Time:   45s
Report: file:///path/to/playwright-report/index.html
```

---

### Backend Tests

#### Unit Tests (JUnit 5)

```bash
cd backend

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=CartServiceTest

# Run with coverage (JaCoCo)
./mvnw test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Test Classes:**
- `CartServiceTest.java` - 14 tests
- `CartServiceMockTest.java` - 7 tests
- `OrderServiceTest.java` - 14 tests
- `OrderServiceMockTest.java` - 10 tests
- `CartControllerMockTest.java` - 6 tests
- `CartControllerIntegrationTest.java` - 3 tests
- `OrderControllerIntegrationTest.java` - 8 tests

**Expected Output:**
```
[INFO] -------------------------------------------------------
[INFO] T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.shopcart.service.CartServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.shopcart.service.OrderServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.shopcart.controller.CartControllerIntegrationTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] -------------------------------------------------------
[INFO] Total: 62 tests, 0 failures
[INFO] -------------------------------------------------------
```

---

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

**Location:** `.github/workflows/ci.yml`

**Triggers:**
- Push to `main` branch
- Pull requests to `main` branch

**Jobs:**

1. **backend-tests** (Ubuntu latest)
   - Setup Java 21
   - Run JUnit 5 tests
   - Run Maven build
   - Time: ~5 minutes

2. **frontend-tests** (Ubuntu latest)
   - Setup Node.js 22
   - Run Vitest unit tests
   - Run ESLint
   - Time: ~3 minutes

3. **e2e-tests** (depends on above)
   - Setup Node.js 22
   - Install Playwright
   - Run Playwright E2E tests
   - Upload HTML report
   - Time: ~10 minutes

**Run Locally:**
```bash
# Simulate CI/CD locally
npm run test        # Frontend unit tests
npm run test:e2e    # Frontend E2E tests
./mvnw test         # Backend tests
```

---

## 📊 Test Coverage

### Frontend

| Module | Tests | Coverage |
|--------|-------|----------|
| `cartValidation.ts` | 7 | 95% |
| `priceCalculation.ts` | 5 | 92% |
| Cart Component | 6 (integration + mocks) | 90% |
| Purchase Component | 7 (integration + mocks) | 88% |
| **Total** | **25+** | **~91%** |

### Backend

| Module | Tests | Coverage |
|--------|-------|----------|
| `CartService` | 21 (14+7) | 90% |
| `OrderService` | 24 (14+10) | 88% |
| `CartController` | 9 (3+6) | 95% |
| `OrderController` | 8 | 92% |
| **Total** | **62+** | **~91%** |

---

## 📝 Test Types

### 1. Unit Tests
- **Frontend:** Vitest
- **Backend:** JUnit 5
- **Coverage:** ≥ 90%

```bash
npm test              # Frontend
./mvnw test          # Backend
```

### 2. Integration Tests
- **Frontend:** React Testing Library
- **Backend:** Spring Test + MockMvc

```bash
npm test Cart.integration.test.tsx
./mvnw test -Dtest=CartControllerIntegrationTest
```

### 3. Mock Tests
- **Frontend:** Vitest with vi.mock()
- **Backend:** Mockito with @MockBean

```bash
npm test Cart.mock.test.tsx
./mvnw test -Dtest=CartControllerMockTest
```

### 4. E2E Tests
- **Tool:** Playwright
- **Browsers:** Chromium, Firefox, WebKit

```bash
npm run test:e2e
npm run test:e2e:ui
```

### 5. Performance Tests
- **Tool:** k6 (recommended) or Apache JMeter
- **Documentation:** `docs/ADVANCED_TESTING.md`

### 6. Security Tests
- **Coverage:** SQL Injection, XSS, IDOR, CSRF, Authentication
- **Documentation:** `docs/ADVANCED_TESTING.md`

---

## 📚 Test Case Documentation

See detailed test cases in: `docs/TESTCASES.md`

**Test Cases:**
- **TC_CART_001-005:** Cart functionality (5 cases)
- **TC_PURCHASE_001-005:** Purchase/Checkout (5 cases)

**Test Scenarios:**
- Happy paths
- Negative tests
- Boundary tests
- Edge cases
- Validation tests

---

## 🔒 Test Data

### Test Users
```
user01: Standard user
user02: Alternative user for testing
```

### Test Products
```
P001: Laptop Dell (15,000,000 VND, 10 stock)
P002: Mouse Logitech (500,000 VND, 50 stock)
P003: Keyboard (2,000,000 VND, 20 stock)
```

### Test Coupons
```
SALE10: 10% discount
SALE20: 20% discount
SAVE500: Fixed 500k discount
EXPIRED2024: Expired coupon (for negative tests)
```

---

## ✅ Test Checklist

Before submitting:

```
Frontend Tests
  ✅ All Vitest unit tests pass
  ✅ E2E tests pass on all 3 browsers
  ✅ Code coverage ≥ 90%
  ✅ No console errors/warnings
  ✅ Lint check passed (ESLint)

Backend Tests
  ✅ All JUnit 5 tests pass
  ✅ Code coverage ≥ 85%
  ✅ Mock tests verify interactions
  ✅ Integration tests validate APIs
  ✅ All endpoints tested

CI/CD
  ✅ GitHub Actions workflow passes
  ✅ HTML reports generated
  ✅ No build failures
  ✅ All branches green

Documentation
  ✅ TESTCASES.md complete
  ✅ ADVANCED_TESTING.md complete
  ✅ README.md updated
  ✅ Code commented
```

---

## 🐛 Troubleshooting

### Frontend Issues

**Playwright browser not found:**
```bash
cd frontend
npx playwright install --with-deps
```

**Vitest not finding modules:**
```bash
npm install
npm test -- --clearCache
```

**Port 5173 already in use:**
```bash
# Change port in playwright.config.ts
baseURL: 'http://localhost:5174'
```

### Backend Issues

**Maven build fails:**
```bash
./mvnw clean install -X
# Check Java version: java -version (should be 21)
```

**Tests timeout:**
```bash
./mvnw test -DargLine="-Dtimeout=60000"
```

**Port 8080 already in use:**
```bash
# Change in application.yaml or run with:
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

## 📚 Additional Resources

- **Vitest Docs:** https://vitest.dev/
- **Playwright Docs:** https://playwright.dev/
- **Spring Boot Testing:** https://spring.io/guides/gs/testing-web/
- **JUnit 5:** https://junit.org/junit5/
- **Mockito:** https://site.mockito.org/

---

## 📅 Test Execution Timeline

| Phase | Duration | Tasks |
|-------|----------|-------|
| Unit Tests | 3-5 min | Frontend + Backend unit tests |
| Integration Tests | 2-3 min | API endpoint tests |
| Mock Tests | 1-2 min | Dependency verification |
| E2E Tests | 10-15 min | Full workflow on 3 browsers |
| CI/CD Pipeline | 20-30 min | Complete automated run |
| **Total** | **~45 min** | All tests pass |

---

## 🎯 Success Criteria

✅ All tests must pass before submission
✅ Code coverage ≥ 85%
✅ Zero critical issues
✅ All browsers supported (Chromium, Firefox, WebKit)
✅ Documentation complete
✅ CI/CD pipeline green

---

**Last Updated:** 09/05/2026  
**Status:** ✅ Ready for Testing  
**Next Review:** After final deployment
