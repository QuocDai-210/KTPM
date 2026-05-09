# ShopCart - E-Commerce Testing Project

Dự án ShopCart là một ứng dụng web thương mại điện tử phục vụ cho việc kiểm thử các chức năng ở cả frontend và backend.

## 📋 Mục Lục

- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Cài Đặt](#cài-đặt)
- [Chạy Ứng Dụng](#chạy-ứng-dụng)
- [Chạy Tests](#chạy-tests)
- [Các Tính Năng](#các-tính-năng)

## 🛠 Công Nghệ Sử Dụng

### Frontend
- React 19.x
- TypeScript
- Vite
- Vitest (Unit Tests)
- React Testing Library
- Playwright (E2E Tests)
- Axios

### Backend
- Spring Boot 4.0.6
- Java 21
- JUnit 5
- Mockito
- Maven
- Spring Data JPA
- Spring Security

## 📂 Cấu Trúc Dự Án

```
ShopCart_FE_BE/
├── frontend/                 # React 19 + Vite application
│   ├── src/
│   │   ├── components/       # Cart, Checkout, Inventory components
│   │   ├── services/         # API services (cartService, orderService)
│   │   ├── utils/            # Validation, price calculation utilities
│   │   ├── hooks/            # Custom React hooks
│   │   └── tests/            # Test files (Vitest)
│   ├── e2e/                  # Playwright E2E tests
│   ├── vite.config.ts
│   ├── playwright.config.ts
│   └── package.json
│
├── backend/
│   ├── src/main/java/com/shopcart/
│   │   ├── controller/        # CartController, ProductController, OrderController
│   │   ├── service/           # Business logic services
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # Database entities
│   │   ├── repository/        # Data access layers
│   │   └── exception/         # Custom exceptions
│   ├── src/test/              # Test files (JUnit 5 + Mockito)
│   ├── performance-tests.k6.js
│   └── pom.xml
│
└── .github/workflows/        # CI/CD pipelines
    └── ci.yml
```

## 💻 Cài Đặt

### Yêu Cầu
- Node.js 22+ (cho frontend)
- Java 21 (cho backend)
- Maven 3.9+

### Frontend Setup

```bash
cd frontend
npm install
```

### Backend Setup

```bash
cd backend
./mvnw clean install
```

## ▶️ Chạy Ứng Dụng

### Frontend Development

```bash
cd frontend
npm run dev
# Truy cập http://localhost:5173
```

### Backend Development

```bash
cd backend
./mvnw spring-boot:run
# API sẽ chạy trên http://localhost:8080
```

## 🧪 Chạy Tests

### Frontend Unit Tests

```bash
cd frontend
npm run test              # Run all tests
npm run test -- --ui      # Run with UI mode
npm run test -- --coverage # Run with coverage report
```

### Frontend E2E Tests

```bash
cd frontend
npm run test:e2e          # Run all E2E tests
npm run test:e2e:all      # Run E2E tests on Chromium, Firefox, and WebKit
npm run test:e2e:ui       # Run with interactive UI
npm run test:e2e:debug    # Run in debug mode
npm run test:e2e:report   # Show HTML report
```

### Backend Unit Tests

```bash
cd backend
./mvnw test               # Run all tests
./mvnw test -Dtest=CartServiceTest  # Run specific test
./mvnw clean package      # Build and run tests
```

### Complete CI/CD Pipeline

```bash
# Local CI/CD simulation
cd backend && ./mvnw test
cd ../frontend && npm install && npm run test -- -- run
npm run test:e2e
```

### Performance Tests

```bash
cd backend
k6 run performance-tests.k6.js
```

### Reports

- Backend coverage: `backend/target/site/jacoco/index.html`
- Frontend coverage: `frontend/coverage/index.html`
- Playwright report: `frontend/playwright-report/index.html`
- Test case documentation: `docs/TESTCASES.md`
- Performance and security documentation: `docs/ADVANCED_TESTING.md`

## 📋 Các Tính Năng

### Cart (Giỏ Hàng)
- ✅ Thêm sản phẩm vào giỏ
- ✅ Xóa sản phẩm khỏi giỏ
- ✅ Cập nhật số lượng
- ✅ Tính tổng giá
- ✅ Kiểm tra tồn kho
- ✅ Validation input

### Purchase (Mua Hàng)
- ✅ Tạo đơn hàng
- ✅ Tính giá với giảm giá
- ✅ Tính phí vận chuyển
- ✅ Kiểm tra coupon
- ✅ Cập nhật tồn kho
- ✅ Xác nhận đơn hàng

## 📊 Test Coverage

### Frontend
- Unit Tests: ≥ 90% coverage
  - `cartValidation.ts`: 10+ test cases
  - `priceCalculation.ts`: 14+ test cases
- Integration Tests: Cart component with mocked services
- E2E Tests: Playwright tests for cart and purchase flows

### Backend
- Unit Tests: ≥ 85% coverage
  - CartService: 6+ test cases
  - OrderService: 8+ test cases
- Integration Tests: Controller API endpoints with MockMvc
- Mock Tests: Service and repository mocking

## 📝 API Endpoints

### Cart Endpoints
- `POST /api/cart/add` - Thêm sản phẩm vào giỏ
- `GET /api/cart/{userId}` - Lấy giỏ hàng
- `PUT /api/cart/update` - Cập nhật số lượng
- `DELETE /api/cart/{userId}/{productId}` - Xóa sản phẩm

### Order Endpoints
- `POST /api/orders` - Tạo đơn hàng
- `GET /api/order/{orderId}` - Lấy thông tin đơn hàng
- `PATCH /api/order/{orderId}/cancel` - Hủy đơn hàng

## 🔐 Authentication

Tất cả API endpoints yêu cầu header `Authorization: Bearer token123`

## 📄 Test Cases Documentation

Xem file `TEST_CASES.md` để tìm:
- Phân tích yêu cầu chức năng
- Chi tiết các test scenarios
- Mức độ ưu tiên (Critical, High, Medium, Low)
- Expected results cho từng scenario

## 🚀 CI/CD Pipeline

GitHub Actions workflow (`ci.yml`) tự động:
1. Chạy backend unit tests (JUnit 5)
2. Chạy frontend unit tests (Vitest)
3. Chạy linting
4. Chạy E2E tests (Playwright)
5. Lưu HTML reports

Pipeline chạy tự động khi push hoặc tạo pull request vào `main` branch.

## 📚 References

- [React Documentation](https://react.dev)
- [Vitest Documentation](https://vitest.dev)
- [Playwright Documentation](https://playwright.dev)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JUnit 5 User Guide](https://junit.org/junit5/)
- [Mockito Framework](https://site.mockito.org/)
