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

#### **TC_CART_003: Add Product - Invalid Quantity (Zero)**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_CART_003 |
| **Test Name** | Thêm sản phẩm với số lượng = 0 - Bị từ chối |
| **Priority** | 🔴 Critical |
| **Category** | Boundary Test |
| **Preconditions** | - Sản phẩm tồn tại và có stock |
| **Test Steps** | 1. Chọn sản phẩm<br>2. Nhập số lượng: 0<br>3. Nhấn "Thêm vào giỏ hàng" |
| **Test Data** | Quantity: 0<br>Stock: 10+ |
| **Expected Result** | - Lỗi: "Số lượng phải lớn hơn 0"<br>- Nút "Thêm vào giỏ hàng" bị disable hoặc không hoạt động<br>- Sản phẩm không được thêm |
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
| **Test Data** | Cart:<br>- P001 (Laptop Dell): 15,000,000 × 2 = 30,000,000<br>- P002 (Mouse): 500,000 × 1 = 500,000<br>Subtotal: 30,500,000 VND<br>Discount: 0 VND<br>Shipping: 50,000 VND<br>**Total: 30,550,000 VND**<br><br>Stock Before:<br>- P001: 10 → After: 8<br>- P002: 50 → After: 49<br><br>Payment: Cash/Credit Card<br>Address: Valid address |
| **Expected Result** | - Order ID generated: ORD-XXXXX<br>- Status: PENDING<br>- Order total = 30,550,000 VND<br>- Stock P001: 10 → 8 ✓<br>- Stock P002: 50 → 49 ✓<br>- Confirmation email sent<br>- Redirect to order detail page<br>- Notification: "Đặt hàng thành công"<br>- Cart cleared |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_002: Apply 10% Coupon - Success**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_002 |
| **Test Name** | Áp dụng mã giảm giá 10% - Tính toán chính xác |
| **Priority** | 🟠 High |
| **Category** | Happy Path |
| **Preconditions** | - Giỏ hàng: 30,500,000 VND<br>- Coupon SALE10 hợp lệ, không hết hạn<br>- Min order: 20,000,000 (đủ điều kiện) |
| **Test Steps** | 1. Mở giỏ hàng<br>2. Nhập mã coupon: "SALE10"<br>3. Nhấn "Áp dụng mã"<br>4. Xác nhận price update |
| **Test Data** | Coupon Code: SALE10<br>Type: PERCENT<br>Value: 10%<br><br>Subtotal: 30,500,000<br>Discount Calc: 30,500,000 × 10% = 3,050,000<br>Shipping: 50,000<br>**Total: 27,500,000** |
| **Expected Result** | - Coupon accepted ✓<br>- Discount shown: -3,050,000 VND<br>- Cart subtotal: 30,500,000 (unchanged)<br>- Discount line: -3,050,000<br>- Shipping: 50,000<br>- **New Total: 27,500,000 VND** ✓<br>- Confirmation message: "Áp dụng mã SALE10 thành công"<br>- Coupon locked (can't remove until uncheck) |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_003: Fixed Amount Coupon - 500k VND**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_003 |
| **Test Name** | Áp dụng mã giảm giá 500k VND - Tính toán chính xác |
| **Priority** | 🟠 High |
| **Category** | Happy Path |
| **Preconditions** | - Giỏ hàng: 30,500,000 VND<br>- Coupon SAVE500 (fixed 500k) hợp lệ |
| **Test Steps** | 1. Nhập coupon: "SAVE500"<br>2. Nhấn "Áp dụng"<br>3. Verify calculation |
| **Test Data** | Coupon: SAVE500<br>Type: FIXED_AMOUNT<br>Value: 500,000 VND<br><br>Subtotal: 30,500,000<br>Discount: 500,000 (fixed)<br>Shipping: 50,000<br>**Total: 30,050,000** |
| **Expected Result** | - Discount applied: -500,000<br>- Subtotal: 30,500,000<br>- Shipping: 50,000<br>- **New Total: 30,050,000 VND** ✓<br>- No further discounts allowed |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_004: Out of Stock During Checkout**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_004 |
| **Test Name** | Đặt hàng khi sản phẩm hết hàng - Bị từ chối |
| **Priority** | 🔴 Critical |
| **Category** | Negative Test |
| **Preconditions** | - Giỏ hàng: P001 x5<br>- Stock P001 trước: 3 (nhỏ hơn qty yêu cầu)<br>- (Simulate: User added 5, but stock changed) |
| **Test Steps** | 1. Mở giỏ hàng (có P001 x5)<br>2. Nhấn "Đặt hàng"<br>3. System check stock realtime |
| **Test Data** | Product P001<br>Requested: 5<br>Available: 3<br>Shortage: 2 items |
| **Expected Result** | - ERROR: "Không đủ tồn kho cho sản phẩm Laptop Dell"<br>- Message: "Chỉ còn 3 chiếc. Vui lòng cập nhật số lượng."<br>- Order REJECTED<br>- Redirect back to cart with warning<br>- Allow user to adjust quantity |
| **Actual Result** | (Để trống) |
| **Status** | Not Run |

---

#### **TC_PURCHASE_005: Invalid/Expired Coupon**

| Field | Value |
|-------|-------|
| **Test Case ID** | TC_PURCHASE_005 |
| **Test Name** | Sử dụng mã giảm giá hết hạn hoặc không hợp lệ - Bị từ chối |
| **Priority** | 🟠 High |
| **Category** | Negative Test |
| **Preconditions** | - Giỏ hàng: 30,500,000 VND |
| **Test Steps** | **Scenario A (Expired):**<br>1. Nhập mã: "EXPIRED2024" (hết hạn 30/04/2026)<br>2. Nhấn "Áp dụng"<br><br>**Scenario B (Invalid):**<br>1. Nhập mã: "FAKE12345" (không tồn tại)<br>2. Nhấn "Áp dụng" |
| **Test Data** | **A)** EXPIRED2024<br>- Valid format<br>- Expiry: 30/04/2026 (< today)<br><br>**B)** FAKE12345<br>- Format valid but not in DB |
| **Expected Result** | **A) Expired Coupon:**<br>- Error: "Mã giảm giá hết hạn (Hạn: 30/04/2026)"<br>- Discount NOT applied<br>- Total remains: 30,550,000<br><br>**B) Invalid Coupon:**<br>- Error: "Mã giảm giá không tồn tại"<br>- Discount NOT applied<br>- Allow retry |
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

**Document Version:** 1.0  
**Last Updated:** 09/05/2026  
**Status:** ✅ Complete
