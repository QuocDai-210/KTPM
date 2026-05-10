# Security Testing

## Phạm vi

Kiểm thử bảo mật tập trung vào các API Cart, Checkout/Order và Products:

- `POST /api/orders`
- `GET /api/order/{orderId}`
- `POST /api/cart/add`
- `PUT /api/cart/update`
- `GET /api/products`

Các request được mô phỏng bằng `MockMvc` trong `backend/src/test/java/com/shopcart/security/SecurityTestSuite.java`.

## Test Cases

| ID | Nhóm rủi ro | Endpoint | Payload / điều kiện | Expected result | Status |
| --- | --- | --- | --- | --- | --- |
| ST_SQLI_001 | SQL Injection | `POST /api/orders` | `productId = P001' OR '1'='1` | API không crash, không tạo order, trả `400 Bad Request` | Pass |
| ST_XSS_001 | XSS | `POST /api/orders` | `shippingAddress = <script>alert('xss')</script>` | API không thực thi script, request được xử lý qua service, response không phản chiếu payload script | Pass |
| ST_CSRF_001 | CSRF/Auth bypass | `POST /api/cart/add` | Không gửi `Authorization` | Từ chối request, trả `401 Unauthorized` | Pass |
| ST_AUTH_002 | Kiểm tra phân quyền API | `POST /api/cart/add` | `Authorization: Bearer invalid-token` | Từ chối request, trả `401 Unauthorized` | Pass |
| ST_IDOR_001 | IDOR | `GET /api/order/{orderId}` | Không gửi `Authorization` | Từ chối truy cập chi tiết order, trả `403 Forbidden` | Pass |
| ST_IDOR_002 | IDOR | `PUT /api/cart/update` | Token `user02` sửa cart của `user01` | Từ chối thao tác, trả `403 Forbidden` và `success=false` | Pass |
| ST_IDOR_003 | IDOR | `GET /api/order/{orderId}` | Token `user02` truy cập order không thuộc quyền sở hữu | Từ chối truy cập, trả `403 Forbidden` | Pass |
| ST_HEADER_001 | Kiểm tra phân quyền API | `GET /api/products` | Không gửi `Authorization` | Từ chối request, trả `401 Unauthorized` | Pass |

## Kết quả thực thi

Lệnh đã chạy:

```bash
cd backend
./mvnw test -Dtest=SecurityTestSuite
```

Kết quả ghi nhận ngày 10/05/2026:

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Phân tích tác động bảo mật

SQL Injection có thể làm lộ dữ liệu sản phẩm/đơn hàng hoặc tạo order sai nếu input được ghép trực tiếp vào truy vấn. Với test hiện tại, payload SQLi không làm API crash và được xử lý như dữ liệu không hợp lệ.

XSS trong địa chỉ giao hàng có thể gây chiếm phiên người dùng hoặc thay đổi nội dung trang nếu hệ thống phản chiếu dữ liệu này ra frontend/email mà không escape. Backend hiện không phản chiếu payload script trong response tạo order, nhưng vẫn cần sanitize/encode khi hiển thị.

IDOR và thiếu phân quyền có thể cho phép người dùng xem/sửa giỏ hàng hoặc đơn hàng của người khác. Các test hiện tại xác nhận API chặn request thiếu token, token sai, và token không khớp chủ sở hữu tài nguyên.

CSRF có thể khiến trình duyệt người dùng gửi request thay đổi dữ liệu ngoài ý muốn. Dự án đang dùng Bearer token trong header, nên request thiếu `Authorization` bị chặn. Nếu chuyển sang cookie session, cần bật lại CSRF protection.

## Đề xuất khắc phục

- Duy trì truy vấn qua repository/parameter binding, không nối chuỗi SQL từ `productId`, `couponCode`, `orderId`.
- Validate whitelist cho các mã nghiệp vụ như `productId`, ví dụ pattern `P[0-9]{3,}`.
- Escape dữ liệu người dùng khi render ở frontend, email hoặc trang admin; cân nhắc sanitize HTML nếu nghiệp vụ cho phép nhập rich text.
- Thay `AuthSupport` hard-code token bằng JWT/session thật, kiểm tra expiry, signature và role.
- Bật `authorizeHttpRequests` theo endpoint thay vì `permitAll`, để rule bảo mật nằm trong Spring Security filter chain.
- Nếu dùng cookie authentication, bật CSRF token cho các method thay đổi dữ liệu như `POST`, `PUT`, `PATCH`, `DELETE`.
- Bổ sung security headers trong môi trường production: CSP chặt hơn, `X-Content-Type-Options`, `Strict-Transport-Security` khi chạy HTTPS.
