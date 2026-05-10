# ShopCart FE/BE

ShopCart là dự án web thương mại điện tử nhỏ dùng cho bài tập kiểm thử phần mềm. Dự án gồm frontend React/Vite và backend Spring Boot, tập trung vào các chức năng giỏ hàng, tính giá, mã giảm giá, tồn kho và đặt hàng.

Mục tiêu chính của dự án là minh họa nhiều tầng kiểm thử:

- Unit test cho logic nghiệp vụ và utility.
- Integration test cho component frontend và API backend.
- Mock test cho service/repository/API dependency.
- E2E automation test với Playwright cho Cart và Purchase/Checkout flow.
- CI/CD bằng GitHub Actions.

## Công Nghệ

Frontend:

- React 19
- TypeScript
- Vite
- Axios
- Vitest
- React Testing Library
- Playwright

Backend:

- Java 21
- Spring Boot
- Maven Wrapper
- JUnit 5
- Mockito
- Spring Data JPA
- Spring Security
- H2/PostgreSQL
- JaCoCo

## Cấu Trúc Dự Án

```text
ShopCart_FE_BE/
├── backend/
│   ├── src/main/java/com/shopcart/
│   │   ├── controller/
│   │   ├── database/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── src/test/java/com/shopcart/
│   ├── mvnw
│   └── pom.xml
│
├── frontend/
│   ├── e2e/
│   │   ├── pages/
│   │   ├── cart.e2e.spec.ts
│   │   └── purchase.e2e.spec.ts
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   ├── tests/
│   │   └── utils/
│   ├── package.json
│   ├── playwright.config.ts
│   └── vite.config.ts
│
└── .github/workflows/
    ├── cart-tests.yml
    └── ci.yml
└── reports/performance/
    ├── performance-tests.k6.js
    ├── cart-k6-summary.md
    ├── cart-k6-summary.json
    └── cart-k6-report.html
```

## Yêu Cầu Môi Trường

- Node.js 22 hoặc mới hơn
- npm
- Java 21
- Maven Wrapper có sẵn trong `backend/`
- Playwright browsers, cài bằng lệnh ở phần dưới

## Cài Đặt

Clone repo và cài dependencies frontend:

```bash
cd frontend
npm install
npx playwright install
```

Chuẩn bị backend:

```bash
cd backend
./mvnw clean test
```

Trên Windows có thể dùng:

```bash
cd backend
mvnw.cmd clean test
```

## Chạy Ứng Dụng

Chạy backend trước:

```bash
cd backend
./mvnw spring-boot:run
```

Backend mặc định chạy tại:

```text
http://localhost:8080
```

Chạy frontend ở terminal khác:

```bash
cd frontend
npm run dev
```

Frontend mặc định chạy tại:

```text
http://localhost:5173
```

Vite đã cấu hình proxy `/api` sang backend `http://localhost:8080`.

## Chạy Tests

Frontend unit/integration tests:

```bash
cd frontend
npm test
```

Frontend coverage:

```bash
cd frontend
npm run test:coverage
```

Frontend lint:

```bash
cd frontend
npm run lint
```

Frontend build:

```bash
cd frontend
npm run build
```

Playwright E2E cho Cart và Purchase trên Chromium + Firefox:

```bash
cd frontend
npm run test:e2e
```

Playwright E2E trên toàn bộ browser đã cấu hình:

```bash
cd frontend
npm run test:e2e:all
```

Chạy riêng Purchase E2E:

```bash
cd frontend
npx playwright test e2e/purchase.e2e.spec.ts --project=chromium
```

Mở Playwright report:

```bash
cd frontend
npm run test:e2e:report
```

Backend tests:

```bash
cd backend
./mvnw test
```

Backend build kèm test:

```bash
cd backend
./mvnw clean package
```

Chạy một test backend cụ thể:

```bash
cd backend
./mvnw test -Dtest=OrderServiceTest
```

Performance test bằng k6:

```bash
docker run --rm --network host \
  -v "$PWD:/work" \
  -w /work \
  grafana/k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e SUMMARY_MD=reports/performance/cart-k6-summary.md \
  -e SUMMARY_JSON=reports/performance/cart-k6-summary.json \
  -e SUMMARY_HTML=reports/performance/cart-k6-report.html \
  reports/performance/performance-tests.k6.js
```

## Chức Năng Chính

- Hiển thị danh sách sản phẩm.
- Thêm sản phẩm vào giỏ hàng.
- Kiểm tra tồn kho và chặn sản phẩm hết hàng.
- Cập nhật/xóa sản phẩm trong giỏ.
- Tính subtotal, discount, shipping fee và total.
- Áp dụng mã giảm giá như `SALE10`, `SALE20`, `FIXED100K`.
- Tạo đơn hàng Purchase/Checkout.
- Xóa giỏ hàng sau khi đặt hàng thành công.

## API Chính

Cart:

- `POST /api/cart/add`
- `GET /api/cart/{userId}`
- `PUT /api/cart/update`
- `DELETE /api/cart/{userId}/{productId}`

Products:

- `GET /api/products`

Orders:

- `POST /api/orders`

Các API test thường dùng header:

```text
Authorization: Bearer token123
```

## Báo Cáo Và Kết Quả Test

Sau khi chạy test, có thể xem các report:

- Frontend coverage: `frontend/coverage/index.html`
- Playwright report: `frontend/playwright-report/index.html`
- Backend JaCoCo report: `backend/target/site/jacoco/index.html`
- Performance report: `reports/performance/cart-k6-summary.md`
- Performance HTML report: `reports/performance/cart-k6-report.html`
- Security report: `reports/security/security-test-summary.md`

Security testing cho câu 6.2:

```bash
cd backend
./mvnw test -Dtest=SecurityTestSuite
```

## CI/CD

Repo có workflow GitHub Actions trong `.github/workflows/` để chạy kiểm thử tự động. Pipeline chính phục vụ các nhóm việc:

- Backend unit/integration tests.
- Frontend unit/integration tests.
- Frontend lint/build.
- Playwright E2E tests.
- Upload test reports khi workflow chạy xong.

## Ghi Chú

- Nếu chỉ chạy Playwright E2E với mock route trong test, không nhất thiết cần backend chạy trước.
- Nếu chạy app thủ công đầy đủ, nên bật backend trước rồi mới bật frontend.
- Khi Playwright thiếu browser binary, chạy lại `npx playwright install` trong thư mục `frontend/`.
