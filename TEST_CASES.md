# ShopCart - Test Cases Documentation

## Câu 1: Phân tích và Thiết kế Test Cases

### 1.1 Giỏ Hàng (Cart) - Phân tích và Test Scenarios

#### Phân tích các yêu cầu chức năng chính của Cart:

1. **Validation rules khi thêm sản phẩm vào giỏ hàng**
   - Kiểm tra Product ID có tồn tại không
   - Kiểm tra sản phẩm có đang được bán không (status = ACTIVE)
   - Kiểm tra số lượng hợp lệ

2. **Validation rules cho số lượng sản phẩm**
   - Số lượng ≥ 1
   - Số lượng ≤ tồn kho hiện tại
   - Kiểu dữ liệu là số nguyên

3. **Kiểm tra tồn kho trước khi thêm vào giỏ**
   - Xác nhận số lượng tồn kho
   - Không vượt quá tồn kho
   - Cảnh báo khi hết hàng

4. **Tính toán tổng giá trị giỏ hàng**
   - Tổng = Σ(đơn giá × số lượng)
   - Cập nhật tổng khi thêm/xóa/cập nhật
   - Làm tròn chính xác đến đồng

5. **Xử lý lỗi**
   - Sản phẩm hết hàng
   - Số lượng âm
   - Số lượng vượt tồn kho
   - Sản phẩm không tồn tại

#### Test Scenarios và Mức độ Ưu tiên:

| Scenario | Priority | Loại | Mô Tả |
|----------|----------|------|-------|
| Thêm sản phẩm hợp lệ vào giỏ thành công | **Critical** | Happy Path | Thêm sản phẩm với số lượng hợp lệ |
| Thêm khi tồn kho không đủ | **Critical** | Negative | Số lượng yêu cầu > tồn kho |
| Thêm sản phẩm hết hàng | **Critical** | Negative | Sản phẩm stock = 0 |
| Thêm với số lượng = 1 (min) | **High** | Boundary | Kiểm tra ranh giới tối thiểu |
| Thêm với số lượng = tồn kho (max) | **High** | Boundary | Kiểm tra ranh giới tối đa |
| Thêm với số lượng = 0 | **High** | Negative | Số lượng không hợp lệ |
| Thêm với số lượng âm | **High** | Negative | Số lượng không hợp lệ |
| Thêm sản phẩm không tồn tại | **High** | Negative | Product ID không hợp lệ |
| Thêm cùng sản phẩm nhiều lần | **Medium** | Edge Case | Cộng dồn số lượng |
| Xóa sản phẩm khỏi giỏ hàng trống | **Medium** | Edge Case | Giỏ không có sản phẩm |
| Cập nhật số lượng sản phẩm | **High** | Positive | Tăng/giảm số lượng |
| Kiểm tra tổng giá sau thêm sản phẩm | **Critical** | Positive | Tính toán giá chính xác |

---

### 1.2 Mua Hàng (Purchase/Checkout) - Phân tích và Test Scenarios

#### Phân tích các yêu cầu chức năng chính của Purchase:

1. **Tính giá**
   - Tổng giá = Σ(đơn giá × số lượng)
   - Áp dụng mã giảm giá (% hoặc số tiền cố định)
   - Cộng phí vận chuyển
   - Tổng cuối = (subtotal - discount) + shipping

2. **Kiểm tra tồn kho trước khi đặt**
   - Xác nhận tất cả sản phẩm trong giỏ còn đủ hàng
   - Không cho phép đặt nếu hết hàng
   - Kiểm tra lại khi checkout

3. **Đặt hàng**
   - Tạo Order entity
   - Trừ tồn kho từ database
   - Gửi xác nhận
   - Tạo mã đơn hàng (Order ID)

4. **Thanh toán**
   - Xác nhận phương thức thanh toán
   - Trạng thái đơn hàng = PENDING
   - Lưu dữ liệu thanh toán

#### Test Scenarios và Mức độ Ưu tiên:

| Scenario | Priority | Loại | Mô Tả |
|----------|----------|------|-------|
| Đặt hàng thành công | **Critical** | Happy Path | Checkout toàn bộ flow |
| Tồn kho được cập nhật sau đặt | **Critical** | Positive | Verify inventory decrease |
| Đặt hàng khi hết hàng | **Critical** | Negative | Không cho phép checkout |
| Tính giá không có giảm giá | **Critical** | Positive | Tính toán chính xác |
| Áp dụng giảm giá % | **High** | Positive | Discount = 10%, 20% |
| Áp dụng giảm giá cố định | **High** | Positive | Discount = 50.000đ |
| Tính phí vận chuyển | **High** | Positive | Shipping = 50.000đ |
| Tổng giá = 0 | **High** | Boundary | Không cho phép đặt |
| Số lượng = 1 (min) | **Medium** | Boundary | Checkout 1 sản phẩm |
| Mã giảm giá hết hạn | **High** | Negative | Không áp dụng discount |
| Mã giảm giá không hợp lệ | **High** | Negative | Từ chối discount không hợp lệ |
| Địa chỉ giao hàng rỗng | **High** | Negative | Bắt buộc nhập địa chỉ |
| Đặt đúng số lượng tồn kho cuối | **High** | Edge Case | Stock = 5, order 5 |

---

## Detailed Test Cases

### TC_CART_001: Thêm sản phẩm vào giỏ hàng thành công

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_CART_001 |
| **Test Name** | Thêm sản phẩm vào giỏ hàng thành công |
| **Priority** | Critical |
| **Preconditions** | - Người dùng đã đăng nhập<br>- Sản phẩm tồn tại và còn hàng<br>- Ứng dụng đang chạy |
| **Test Steps** | 1. Truy cập trang danh sách sản phẩm<br>2. Chọn sản phẩm "Laptop Dell"<br>3. Nhập số lượng: 2<br>4. Nhấn nút "Thêm vào giỏ hàng" |
| **Test Data** | - Product ID: P001<br>- Product Name: Laptop Dell<br>- Quantity: 2<br>- Price: 15.000.000đ<br>- Stock available: 10 |
| **Expected Result** | - Thông báo "Thêm vào giỏ hàng thành công"<br>- Số lượng trong giỏ hàng = 2<br>- Icon giỏ hàng hiển thị badge = 2<br>- Tồn kho tạm thời giảm còn 8 |
| **Actual Result** | (Để trống - sinh viên điền) |
| **Status** | Not Run |

---

### TC_CART_002: Thêm sản phẩm vượt quá tồn kho

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_CART_002 |
| **Test Name** | Thêm sản phẩm vượt quá tồn kho |
| **Priority** | Critical |
| **Preconditions** | - Người dùng đã đăng nhập<br>- Sản phẩm tồn tại<br>- Tồn kho: 5 sản phẩm |
| **Test Steps** | 1. Truy cập trang danh sách sản phẩm<br>2. Chọn sản phẩm<br>3. Nhập số lượng: 10 (vượt quá stock)<br>4. Nhấn "Thêm vào giỏ hàng" |
| **Test Data** | - Product ID: P002<br>- Quantity: 10<br>- Stock available: 5 |
| **Expected Result** | - Thông báo lỗi "Số lượng vượt quá tồn kho"<br>- Gợi ý số lượng tối đa: 5<br>- Sản phẩm không được thêm vào giỏ<br>- Giỏ hàng không thay đổi |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_CART_003: Thêm sản phẩm hết hàng

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_CART_003 |
| **Test Name** | Thêm sản phẩm hết hàng |
| **Priority** | Critical |
| **Preconditions** | - Sản phẩm tồn tại nhưng hết hàng<br>- Stock = 0 |
| **Test Steps** | 1. Truy cập trang danh sách sản phẩm<br>2. Chọn sản phẩm hết hàng<br>3. Nút "Thêm vào giỏ" bị vô hiệu hóa hoặc<br>4. Cố gắng thêm vào giỏ |
| **Test Data** | - Product ID: P003<br>- Stock: 0<br>- Status: ACTIVE (nhưng hết hàng) |
| **Expected Result** | - Nút "Thêm vào giỏ" bị disable<br>- Hiển thị thông báo "Sản phẩm hết hàng"<br>- Không thể thêm vào giỏ |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_CART_004: Cập nhật số lượng sản phẩm trong giỏ

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_CART_004 |
| **Test Name** | Cập nhật số lượng sản phẩm trong giỏ |
| **Priority** | High |
| **Preconditions** | - Giỏ hàng có sản phẩm (qty=2)<br>- Sản phẩm còn hàng |
| **Test Steps** | 1. Mở giỏ hàng<br>2. Tìm sản phẩm đã thêm<br>3. Thay đổi số lượng: 2 → 5<br>4. Lưu thay đổi |
| **Test Data** | - Product ID: P001<br>- Current Qty: 2<br>- New Qty: 5<br>- Stock: 10 |
| **Expected Result** | - Số lượng cập nhật = 5<br>- Tổng giá được tính lại<br>- Badge giỏ hàng = 5 |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_CART_005: Xóa sản phẩm khỏi giỏ hàng

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_CART_005 |
| **Test Name** | Xóa sản phẩm khỏi giỏ hàng |
| **Priority** | High |
| **Preconditions** | - Giỏ hàng có sản phẩm |
| **Test Steps** | 1. Mở giỏ hàng<br>2. Nhấn nút "Xóa" trên sản phẩm<br>3. Xác nhận xóa |
| **Test Data** | - Product ID: P001<br>- Current Qty in Cart: 2 |
| **Expected Result** | - Sản phẩm được xóa khỏi giỏ<br>- Tổng giá được cập nhật<br>- Badge giỏ hàng giảm<br>- Thông báo "Sản phẩm đã được xóa" |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_PURCHASE_001: Đặt hàng thành công

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_PURCHASE_001 |
| **Test Name** | Đặt hàng thành công và tồn kho được cập nhật |
| **Priority** | Critical |
| **Preconditions** | - Người dùng đã đăng nhập<br>- Giỏ hàng có ít nhất 1 sản phẩm<br>- Tất cả sản phẩm còn đủ hàng |
| **Test Steps** | 1. Truy cập trang Giỏ hàng<br>2. Xác nhận thông tin đơn hàng<br>3. Chọn phương thức thanh toán<br>4. Nhập địa chỉ giao hàng<br>5. Nhấn "Đặt hàng" |
| **Test Data** | - Product: Laptop Dell (P001)<br>  - Qty: 2<br>  - Price: 15.000.000đ<br>- Product: Mouse Logitech (P002)<br>  - Qty: 1<br>  - Price: 500.000đ<br>- Coupon: SALE10 (10% discount)<br>- Shipping: 50.000đ |
| **Expected Result** | - Order tạo thành công<br>- Order ID được tạo (ORD-xxxxx)<br>- Status: PENDING<br>- Tổng tiền = (15M×2 + 500K×1)×0.9 + 50K = 27.950.000đ<br>- Tồn kho P001: 10 → 8<br>- Tồn kho P002: 50 → 49<br>- Email xác nhận được gửi<br>- Chuyển đến trang "Order Confirmation" |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_PURCHASE_002: Kiểm tra tính toán giá sai

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_PURCHASE_002 |
| **Test Name** | Kiểm tra tính toán giá chính xác với mã giảm giá |
| **Priority** | Critical |
| **Preconditions** | - Giỏ hàng có sản phẩm<br>- Mã giảm giá hợp lệ |
| **Test Steps** | 1. Mở trang Checkout<br>2. Nhập mã giảm giá "SALE20"<br>3. Kiểm tra tính toán giá |
| **Test Data** | - Subtotal: 10.000.000đ<br>- Coupon SALE20: 20%<br>- Shipping: 50.000đ |
| **Expected Result** | - Discount = 10M × 20% = 2.000.000đ<br>- Total = (10M - 2M) + 50K = 8.050.000đ<br>- Tính toán được hiển thị chính xác |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_PURCHASE_003: Đặt hàng khi hết hàng

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_PURCHASE_003 |
| **Test Name** | Không cho phép đặt hàng khi sản phẩm hết |
| **Priority** | Critical |
| **Preconditions** | - Giỏ hàng có sản phẩm (qty=5)<br>- Sản phẩm hết hàng sau khi thêm |
| **Test Steps** | 1. Thêm sản phẩm vào giỏ<br>2. Sản phẩm bị sold out<br>3. Truy cập Checkout<br>4. Nhấn "Đặt hàng" |
| **Test Data** | - Product: P001<br>- Requested Qty: 5<br>- Current Stock: 0 |
| **Expected Result** | - Thông báo lỗi: "Sản phẩm này đã hết hàng"<br>- Không tạo Order<br>- Gợi ý remove sản phẩm khỏi giỏ |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_PURCHASE_004: Mã giảm giá hết hạn

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_PURCHASE_004 |
| **Test Name** | Không áp dụng mã giảm giá hết hạn |
| **Priority** | High |
| **Preconditions** | - Mã giảm giá đã hết hạn (expiry_date < ngày hôm nay) |
| **Test Steps** | 1. Mở Checkout<br>2. Nhập mã giảm giá hết hạn<br>3. Nhấn "Áp dụng" |
| **Test Data** | - Coupon: EXPIRED2023<br>- Expiry Date: 31/12/2023 |
| **Expected Result** | - Thông báo: "Mã giảm giá đã hết hạn"<br>- Discount = 0<br>- Tính toán lại tổng tiền không discount |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

### TC_PURCHASE_005: Địa chỉ giao hàng rỗng

| Trường | Nội dung |
|-------|---------|
| **Test Case ID** | TC_PURCHASE_005 |
| **Test Name** | Không cho phép đặt hàng khi địa chỉ rỗng |
| **Priority** | High |
| **Preconditions** | - Form Checkout hiển thị |
| **Test Steps** | 1. Mở trang Checkout<br>2. Để trống field "Địa chỉ giao hàng"<br>3. Nhấn "Đặt hàng" |
| **Test Data** | - Address: (empty) |
| **Expected Result** | - Validation error: "Vui lòng nhập địa chỉ giao hàng"<br>- Không tạo Order<br>- Focus vào field Address |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

