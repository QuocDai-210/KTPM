# 📋 TEST CASES - ShopCart Project

**Version:** 1.1  
**Author:** QA Team  
**Date:** May 2026  
**Subject:** Cart & Purchase Test Cases

---

## 📑 Table of Contents

- [Cart Test Cases (Câu 1.1)](#cart-test-cases)
  - [Analysis & Scenarios](#11-analysis--scenarios)
  - [Test Cases (TC_CART_001-005)](#12-test-cases-detailed)
- [Purchase Test Cases (Câu 1.2)](#purchase-test-cases)
  - [Analysis & Scenarios](#21-analysis--scenarios)
  - [Test Cases (TC_PURCHASE_001-005)](#22-test-cases-detailed)

---

## 🛒 CART TEST CASES

### 1.1 Analysis & Scenarios

#### Functional Requirements:
- **Add Product:** User selects and adds products to cart with quantity validation
- **Quantity Rules:** ≥ 1, ≤ stock available, integer type only
- **Inventory Check:** Verify stock before adding
- **Total Calculation:** Calculate cart total (price × qty)
- **Product Status:** Only ACTIVE products can be added
- **Remove/Update:** Delete items from cart or update quantities

#### Test Scenarios:
| Scenario | Type | Priority | Description |
|----------|------|----------|-------------|
| Add valid product | Happy Path | Critical | Add 1-10 items successfully |
| Quantity = 0 | Boundary | Critical | Reject 0 quantity |
| Negative quantity | Negative | Critical | Reject negative numbers |
| Exceed stock | Negative | Critical | Prevent exceeding available stock |
| Out of stock | Negative | Critical | Block adding out-of-stock items |
| Add same product twice | Edge Case | High | Accumulate quantities |
| Remove from cart | Happy Path | High | Remove item successfully |
| Remove from empty cart | Edge Case | Medium | Handle gracefully |
| Update quantity | Happy Path | Medium | Modify item quantity |
| Max quantity (boundary) | Boundary | Medium | Add max available stock |

#### Validation Rules:
```
Quantity (Số lượng):
  - ≥ 1 (minimum)
  - ≤ stock (current available)
  - Integer only (no decimals)
  - Required (cannot be null/undefined)

Product ID:
  - Must exist in system
  - Not null/empty
  - Format: P###

Stock (Tồn kho):
  - Cart quantity ≤ available stock
  - Real-time validation
  - Update after purchase

Product Status:
  - Must be ACTIVE
  - INACTIVE products blocked
```

---

### 1.2 Test Cases Detailed

#### **TC_CART_001: Add Valid Product to Cart - Success**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_CART_001 |
| **Test Name** | Thêm sản phẩm hợp lệ vào giỏ hàng thành công |
| **Priority** | 🔴 Critical |
| **Category** | Happy Path |
| **Preconditions** | - Người dùng đã đăng nhập<br>- Sản phẩm P001 tồn tại và còn hàng (stock=10)<br>- Giỏ hàng rỗng<br>- Ứng dụng đang chạy |
| **Test Steps** | 1. Truy cập trang danh sách sản phẩm<br>2. Chọn sản phẩm "Laptop Dell" (P001)<br>3. Nhập số lượng: 2<br>4. Nhấn nút "Thêm vào giỏ hàng" |
| **Test Data** | Product ID: P001<br>Product Name: Laptop Dell<br>Unit Price: 15,000,000 VND<br>Quantity: 2<br>Stock Available: 10<br>Expected Cart Total: 30,000,000 VND |
| **Expected Result** | - Thông báo thành công: "Thêm vào giỏ hàng thành công"<br>- Số lượng trong giỏ hàng = 2<br>- Icon giỏ hàng hiển thị badge = 2<br>- Tồn kho tạm thời giảm từ 10 → 8<br>- Cart total = 30,000,000 VND<br>- Redirect to cart hoặc stay on products page |
| **Actual Result** | (Để trống - sinh viên điền sau khi chạy) |
| **Status** | Not Run |
| **Notes** | Negative test: Should reject if stock < quantity |

---

#### **TC_CART_002: Add Product - Exceed Stock**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_CART_002 |
| **Test Name** | Thêm sản phẩm vượt quá tồn kho - Bị từ chối |
| **Priority** | 🔴 Critical |
| **Category** | Negative Test |
| **Preconditions** | - Sản phẩm P002 có stock = 5<br>- Giỏ hàng rỗng |
| **Test Steps** | 1. Chọn sản phẩm "Mouse Logitech" (P002)<br>2. Nhập số lượng: 8 (vượt stock)<br>3. Nhấn "Thêm vào giỏ hàng" |
| **Test Data** | Product ID: P002<br>Product Name: Mouse Logitech<br>Unit Price: 500,000 VND<br>Requested Qty: 8<br>Stock Available: 5 |
| **Expected Result** | - Hiển thị lỗi: "Số lượng không được vượt quá tồn kho (Tối đa: 5)"<br>- Sản phẩm KHÔNG được thêm vào giỏ hàng<br>- Giỏ hàng vẫn rỗng<br>- Input field làm sáng đỏ hoặc có icon lỗi |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_CART_003: Add Product - Out of Stock**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_CART_003 |
| **Test Name** | Thêm sản phẩm hết hàng vào giỏ - Bị từ chối |
| **Priority** | 🔴 Critical |
| **Category** | Negative Test |
| **Preconditions** | - Sản phẩm P003 tồn tại<br>- P003 đang ACTIVE nhưng stock = 0<br>- Giỏ hàng rỗng |
| **Test Steps** | 1. Truy cập trang danh sách sản phẩm<br>2. Chọn sản phẩm "Keyboard Mechanical" (P003)<br>3. Nhập số lượng: 1<br>4. Nhấn "Thêm vào giỏ hàng" |
| **Test Data** | Product ID: P003<br>Product Name: Keyboard Mechanical<br>Quantity: 1<br>Stock Available: 0 |
| **Expected Result** | - Hiển thị lỗi: "Sản phẩm đã hết hàng"<br>- Nút "Thêm vào giỏ hàng" bị disable hoặc thao tác bị từ chối<br>- Sản phẩm KHÔNG được thêm vào giỏ hàng<br>- Giỏ hàng vẫn rỗng<br>- Badge giỏ hàng không thay đổi |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_CART_004: Remove Product from Cart - Success**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_CART_004 |
| **Test Name** | Xóa sản phẩm khỏi giỏ hàng thành công |
| **Priority** | 🟠 High |
| **Category** | Happy Path |
| **Preconditions** | - Giỏ hàng có 2 sản phẩm<br>- Sản phẩm P001 (Laptop Dell) x2 trong giỏ |
| **Test Steps** | 1. Mở giỏ hàng<br>2. Tìm sản phẩm "Laptop Dell"<br>3. Nhấn nút "Xóa" hoặc icon trash<br>4. Xác nhận xóa nếu có dialog |
| **Test Data** | Product to Remove: P001 (Laptop Dell)<br>Cart Before: 2 items<br>Stock Before: 8 (after add) |
| **Expected Result** | - Sản phẩm bị xóa khỏi giỏ hàng<br>- Số lượng trong giỏ hàng giảm từ 2 → 0<br>- Badge cập nhật = 0 (hoặc ẩn)<br>- Tồn kho P001 hoàn trả: 8 → 10<br>- Cart total = 0 VND<br>- Thông báo: "Xóa thành công" |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_CART_005: Update Product Quantity in Cart**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_CART_005 |
| **Test Name** | Cập nhật số lượng sản phẩm trong giỏ hàng |
| **Priority** | 🟠 High |
| **Category** | Happy Path |
| **Preconditions** | - Giỏ hàng có P001 x2 (total: 30M VND)<br>- Stock P001: 8 (remaining after add) |
| **Test Steps** | 1. Mở giỏ hàng<br>2. Tìm sản phẩm Laptop Dell<br>3. Thay đổi số lượng từ 2 → 3 (hoặc dùng +/- button)<br>4. System tự lưu hoặc nhấn "Cập nhật" |
| **Test Data** | Product: P001 (15M each)<br>Old Qty: 2 → New Qty: 3<br>Old Total: 30M → New Total: 45M<br>Stock Before: 8 → Stock After: 7 |
| **Expected Result** | - Số lượng cập nhật: 2 → 3<br>- Cart total tính lại: 45,000,000 VND<br>- Tồn kho giảm: 8 → 7<br>- Badge cập nhật = 3<br>- Thông báo: "Cập nhật giỏ hàng thành công"<br>- Không vượt stock |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

## 🛍️ PURCHASE TEST CASES

### 2.1 Analysis & Scenarios

#### Functional Requirements:
- **Price Calculation:** Calculate total = (price × qty) for all items, apply coupon, add shipping
- **Coupon Validation:** Verify valid, not expired, not used, conditions met
- **Inventory Check:** Confirm all items have sufficient stock before checkout
- **Order Confirmation:** Create order, deduct stock, generate order ID, return status
- **Payment:** Confirm payment method selection
- **Address Validation:** Address not empty, valid format

#### Test Scenarios:
| Scenario | Type | Priority | Description |
|----------|------|----------|-------------|
| Successful checkout | Happy Path | Critical | Complete order with valid data |
| Correct price calculation | Happy Path | Critical | Subtotal + Discount + Shipping |
| Apply percentage coupon | Happy Path | High | 10%, 20% discount |
| Apply fixed coupon | Happy Path | High | Fixed amount discount |
| Out of stock during checkout | Negative | Critical | Prevent order if stock changed |
| Expired coupon | Negative | High | Reject expired codes |
| Invalid coupon | Negative | High | Reject non-existent codes |
| Empty address | Negative | High | Reject empty delivery address |
| Exact stock quantity | Boundary | Medium | Order exactly available stock |
| Multiple items calculation | Happy Path | Medium | Complex price calc with mix |

#### Validation Rules:
```
Total Price:
  - > 0 (must be positive)
  - Formula: SUM(price × qty) - discount + shipping
  - Accurate to 1 decimal place (VND)

Coupon Code:
  - Valid format
  - Not expired (expiry_date > now)
  - Not yet used
  - Conditions met (min order, product type, etc.)
  - Types: PERCENT or FIXED_AMOUNT

Stock Availability:
  - All items must have ≥ requested qty
  - Check at checkout time
  - Prevent overselling

Address:
  - Not empty/null
  - Valid format
  - Valid province/district combo

Payment Method:
  - Must be selected
  - Valid payment method
```

---

### 2.2 Test Cases Detailed

#### **TC_PURCHASE_001: Checkout Success & Stock Deduction**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_001 |
| **Test Name** | Đặt hàng thành công và tồn kho được cập nhật |
| **Priority** | 🔴 Critical |
| **Category** | Happy Path |
| **Preconditions** | - Người dùng đã đăng nhập<br>- Giỏ hàng có: P001 x2 + P002 x1<br>- Stock P001=10, P002=50<br>- Payment method selected<br>- Address filled |
| **Test Steps** | 1. Truy cập trang Giỏ hàng<br>2. Xác nhận thông tin đơn hàng<br>3. Chọn phương thức thanh toán<br>4. Nhập địa chỉ giao hàng<br>5. Nhấn "Đặt hàng" |
| **Test Data** | Cart:<br>- P001 (Laptop Dell): 15,000,000 × 2 = 30,000,000<br>- P002 (Mouse): 500,000 × 1 = 500,000<br>Subtotal: 30,500,000 VND<br>Coupon: SALE10 (giảm 10%)<br>Discount: 3,050,000 VND<br>Shipping: 50,000 VND<br>**Total: 27,500,000 VND**<br><br>Stock Before:<br>- P001: 10 → After: 8<br>- P002: 50 → After: 49<br><br>Payment: Cash/Credit Card<br>Address: Valid address |
| **Expected Result** | - Order ID generated: ORD-XXXXX<br>- Status: PENDING<br>- Order total = 27,500,000 VND<br>- Stock P001: 10 → 8 ✓<br>- Stock P002: 50 → 49 ✓<br>- Confirmation email sent<br>- Redirect to order detail page<br>- Notification: "Đặt hàng thành công"<br>- Cart cleared |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_002: Checkout With Out-of-Stock Product**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_002 |
| **Test Name** | Đặt hàng khi sản phẩm hết hàng - Bị từ chối |
| **Priority** | 🔴 Critical |
| **Category** | Negative Test |
| **Preconditions** | - Người dùng đã đăng nhập<br>- Giỏ hàng có P003 x1<br>- Stock P003 tại thời điểm checkout = 0 |
| **Test Steps** | 1. Truy cập trang Giỏ hàng<br>2. Xác nhận thông tin đơn hàng<br>3. Chọn phương thức thanh toán<br>4. Nhấn "Đặt hàng" |
| **Test Data** | Product ID: P003<br>Product Name: Keyboard Mechanical<br>Requested Qty: 1<br>Available Stock: 0<br>Shipping: 50,000 VND |
| **Expected Result** | - Hiển thị lỗi: "Sản phẩm Keyboard Mechanical đã hết hàng"<br>- Order KHÔNG được tạo<br>- Không phát sinh orderId<br>- Tồn kho không thay đổi<br>- Giỏ hàng vẫn giữ sản phẩm để người dùng xóa/cập nhật |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_003: Detect Incorrect Price Calculation**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_003 |
| **Test Name** | Kiểm tra phát hiện tổng tiền tính sai |
| **Priority** | 🔴 Critical |
| **Category** | Negative Test |
| **Preconditions** | - Giỏ hàng có nhiều sản phẩm<br>- Backend/API trả về hoặc UI hiển thị tổng tiền khác công thức chuẩn |
| **Test Steps** | 1. Truy cập trang Checkout<br>2. Kiểm tra subtotal từng dòng sản phẩm<br>3. Áp dụng coupon SALE10<br>4. Kiểm tra shipping fee<br>5. Đối chiếu tổng tiền hiển thị với công thức chuẩn |
| **Test Data** | P001: 15,000,000 × 2 = 30,000,000<br>P002: 500,000 × 1 = 500,000<br>Subtotal đúng: 30,500,000<br>Coupon SALE10: -3,050,000<br>Shipping: 50,000<br>Expected Total: 27,500,000<br>Faulty Total giả lập: 27,950,000 |
| **Expected Result** | - Hệ thống/test phát hiện sai lệch tổng tiền<br>- Không cho đặt hàng nếu tổng tiền request khác tổng tiền server tính lại<br>- Hiển thị hoặc ghi nhận lỗi: "Tổng tiền không hợp lệ"<br>- Không trừ tồn kho, không tạo order |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_004: Expired Coupon**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_004 |
| **Test Name** | Sử dụng mã giảm giá hết hạn - Bị từ chối |
| **Priority** | 🟠 High |
| **Category** | Negative Test |
| **Preconditions** | - Giỏ hàng: 30,500,000 VND<br>- Coupon EXPIRED2024 tồn tại nhưng đã hết hạn |
| **Test Steps** | 1. Truy cập trang Checkout<br>2. Nhập mã giảm giá: "EXPIRED2024"<br>3. Nhấn "Áp dụng"<br>4. Kiểm tra tổng tiền và thông báo lỗi |
| **Test Data** | Coupon: EXPIRED2024<br>Expiry Date: 30/04/2026<br>Current Date: 09/05/2026<br>Subtotal: 30,500,000<br>Shipping: 50,000 |
| **Expected Result** | - Hiển thị lỗi: "Mã giảm giá đã hết hạn"<br>- Discount không được áp dụng<br>- Total giữ nguyên = 30,550,000 VND<br>- Người dùng có thể nhập mã khác<br>- Order chưa được tạo ở bước áp dụng coupon |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_005: Insufficient Stock During Checkout**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_005 |
| **Test Name** | Đặt hàng khi tồn kho không đủ - Bị từ chối |
| **Priority** | 🔴 Critical |
| **Category** | Negative Test |
| **Preconditions** | - Giỏ hàng: P001 x5<br>- Stock P001 tại thời điểm thêm vào giỏ có thể đủ, nhưng trước checkout chỉ còn 3 |
| **Test Steps** | 1. Mở giỏ hàng có P001 x5<br>2. Chọn phương thức thanh toán<br>3. Nhập địa chỉ giao hàng hợp lệ<br>4. Nhấn "Đặt hàng"<br>5. Hệ thống kiểm tra tồn kho realtime |
| **Test Data** | Product ID: P001<br>Requested Qty: 5<br>Available Stock: 3<br>Shortage: 2<br>Unit Price: 15,000,000 VND |
| **Expected Result** | - Hiển thị lỗi: "Không đủ tồn kho cho sản phẩm Laptop Dell. Chỉ còn 3 sản phẩm"<br>- Order bị từ chối<br>- Không trừ tồn kho<br>- Không gửi email xác nhận<br>- Giỏ hàng giữ nguyên P001 x5 và yêu cầu người dùng cập nhật số lượng |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

## 📊 Test Case Summary

### Cart Tests
- **Total:** 5 test cases
- **Critical:** 3 (TC_CART_001, 002, 003)
- **High:** 2 (TC_CART_004, 005)
- **Coverage:** Add, Remove, Update, Validation, Boundary

### Purchase Tests
- **Total:** 5 test cases
- **Critical:** 2 (TC_PURCHASE_001, 004)
- **High:** 3 (TC_PURCHASE_002, 003, 005)
- **Coverage:** Checkout, Pricing, Coupons, Stock, Validation

### Overall
- **Total Test Cases:** 10
- **Critical:** 5 (50%)
- **High:** 5 (50%)
- **Estimated Execution Time:** 4-5 hours (manual)

---

## 🔍 Validation Rules Reference

### Cart Validation
```javascript
validateCartItem({
  productId: 'P###',      // Required, exists in DB
  quantity: 1-stock,       // Min=1, Max=stock, integer
  stock: number            // Current available stock
})

// Returns:
{
  valid: boolean,
  error?: string          // If invalid
}
```

### Purchase Validation
```javascript
validateOrder({
  items: CartItem[],
  coupon?: CouponCode,
  shipping: number,
  address: Address
})

// Checks:
// - stock available for all items
// - coupon valid & not expired
// - address not empty
// - total > 0
```

---

**Document Version:** 1.1  
**Last Updated:** 09/05/2026  
**Status:** ✅ Complete
