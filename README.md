# ShopCart FE/BE

ShopCart là ứng dụng web thương mại điện tử phục vụ bài tập kiểm thử phần mềm. Dự án gồm frontend React/Vite, backend Spring Boot và các bộ test cho unit, integration, E2E, security, performance.

## Công Nghệ Sử Dụng

| Phần | Công nghệ |
| --- | --- |
| Frontend | React 19, TypeScript, Vite, Axios |
| Frontend test | Vitest, React Testing Library, Playwright |
| Backend | Java 21, Spring Boot 4, Spring Data JPA, Spring Security |
| Backend test | JUnit 5, Mockito, MockMvc, JaCoCo |
| Database | H2 in-memory, PostgreSQL |
| Performance test | k6 |
| Container | Docker, Docker Compose, Nginx |

## Chức Năng Chính

- Xem danh sách sản phẩm.
- Thêm sản phẩm vào giỏ hàng.
- Cập nhật hoặc xóa sản phẩm trong giỏ hàng.
- Kiểm tra tồn kho trước khi thêm vào giỏ hoặc đặt hàng.
- Tính subtotal, giảm giá, phí vận chuyển và tổng tiền.
- Tạo, xem và hủy đơn hàng.
- Kiểm thử bảo mật cho các rủi ro SQL Injection, XSS, CSRF/Auth bypass, IDOR.
- Kiểm thử hiệu năng Cart API bằng k6.

## Cấu Trúc Thư Mục

```text
ShopCart_FE_BE/
├── backend/
│   ├── src/main/java/com/shopcart/
│   ├── src/main/resources/
│   ├── src/test/java/com/shopcart/
│   ├── Dockerfile
│   ├── mvnw
│   └── pom.xml
├── frontend/
│   ├── e2e/
│   ├── src/
│   ├── Dockerfile
│   ├── package.json
│   ├── playwright.config.ts
│   └── vite.config.ts
├── reports/
│   ├── performance/
│   └── security/
└── docker-compose.yml
```

## Yêu Cầu Môi Trường

Dự án đã được Docker hóa, nên để cài đặt và chạy toàn bộ ứng dụng chỉ cần:

- Docker.
- Docker Compose.

Các công cụ sau chỉ cần khi muốn chạy frontend/backend trực tiếp trên máy hoặc chạy test cục bộ ngoài container:

- Node.js 22 hoặc mới hơn.
- npm.
- Java 21.

Backend đã có Maven Wrapper trong `backend/`, vì vậy không bắt buộc cài Maven global.

## Cài Đặt

## Chạy Bằng Docker 

Từ thư mục gốc repo:

```bash
docker compose up --build
```

Docker Compose sẽ chạy:

| Service | Port | 
| --- | --- |
| `frontend` | `http://localhost:5173` |
| `backend` | `http://localhost:8080` |
| `postgres` | `localhost:5432` |

Dừng container:

```bash
docker compose down
```

## Chạy Test

### Frontend Unit/Integration Test

Chạy Vitest và xuất coverage:

```bash
cd frontend
npm run test:coverage
```

### Frontend E2E Test

Chạy E2E Test:

```bash
cd frontend
npx playwright install
npm run test:e2e
```

Xem kết quả test E2E:

```bash
npx playwright show-report
```

### Backend Test

Chạy toàn bộ test backend:

```bash
cd backend
./mvnw test
```

### Performance Test Bằng k6

Từ thư mục gốc repo, chạy k6 bằng Docker:

```bash
docker run --rm --network host \
  -v "$PWD:/work" \
  -w /work \
  grafana/k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e AUTH_TOKEN=token123 \
  -e USER_ID=user01 \
  -e PRODUCT_ID=P010 \
  -e SUMMARY_MD=reports/performance/cart-k6-summary.md \
  -e SUMMARY_JSON=reports/performance/cart-k6-summary.json \
  -e SUMMARY_HTML=reports/performance/cart-k6-report.html \
  reports/performance/performance-tests.k6.js
```
