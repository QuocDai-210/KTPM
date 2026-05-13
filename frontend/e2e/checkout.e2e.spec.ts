import { expect, test } from '@playwright/test';
import CheckoutPage from './pages/CheckoutPage';

const cartItem = {
  productId: 'P001',
  productName: 'Laptop Dell',
  quantity: 1,
  price: 15000000,
};

test.describe('Checkout E2E Tests', () => {
  let checkoutPage: CheckoutPage;
  let removeCartItemCalls: number;

  test.beforeEach(async ({ page }) => {
    checkoutPage = new CheckoutPage(page);
    removeCartItemCalls = 0;

    await page.route('**/api/products', async (route) => {
      await route.fulfill({
        json: [{ id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 }],
      });
    });

    await page.route('**/api/cart/user01', async (route) => {
      await route.fulfill({
        json: {
          success: true,
          items: [cartItem],
          itemCount: 1,
          cartTotal: 15000000,
        },
      });
    });

    await page.route('**/api/cart/user01/P001', async (route) => {
      removeCartItemCalls += 1;
      await route.fulfill({
        json: {
          success: true,
          items: [],
          itemCount: 0,
          cartTotal: 0,
        },
      });
    });

    await page.route('**/api/orders', async (route) => {
      await route.fulfill({
        status: 201,
        json: { orderId: 'ORD-CHECKOUT', status: 'PENDING', totalPrice: 15050000 },
      });
    });

    await page.goto('/cart');
    await expect(checkoutPage.placeOrderBtn).toBeVisible();
  });

  test('Checkout flow - complete success path', async ({ page }) => {
    await expect(checkoutPage.cartItem('Laptop Dell')).toBeVisible();
    await checkoutPage.goToCheckout();
    await checkoutPage.fillShippingAddress('123 Nguyen Trai, HCM');
    await checkoutPage.placeOrder();

    await expect(checkoutPage.successMessage).toContainText('ORD-CHECKOUT');
    await expect(checkoutPage.emptyCartMessage).toBeVisible();
    await expect(page).toHaveURL(/\/cart/);
    expect(removeCartItemCalls).toBe(1);
  });

  test('Price calculation - verify subtotal, shipping and total', async () => {
    await expect(checkoutPage.cartSubtotal).toContainText('15.000.000');
    await expect(checkoutPage.discountAmount).toContainText('0');
    await expect(checkoutPage.shippingFee).toContainText('50.000');
    await expect(checkoutPage.checkoutTotal).toContainText('15.050.000');
  });

  test('Apply coupon - 10% discount', async () => {
    await checkoutPage.applyCoupon('SALE10');

    await expect(checkoutPage.formMessage).toContainText('SALE10');
    await expect(checkoutPage.discountAmount).toContainText('1.500.000');
    await expect(checkoutPage.checkoutTotal).toContainText('13.550.000');
  });

  test('Invalid coupon - show validation error and clear discount', async () => {
    await checkoutPage.applyCoupon('UNKNOWN');

    await expect(checkoutPage.formMessage).toContainText('Mã giảm giá không hợp lệ');
    await expect(checkoutPage.discountAmount).toContainText('0');
    await expect(checkoutPage.checkoutTotal).toContainText('15.050.000');
  });

  test('Invalid address - show validation error', async () => {
    await checkoutPage.shippingAddressInput.clear();
    await checkoutPage.placeOrderBtn.click();

    await expect(checkoutPage.formMessage).toContainText('Vui lòng nhập địa chỉ giao hàng');
    await expect(checkoutPage.cartItem('Laptop Dell')).toBeVisible();
  });

  test('Out of stock warning - Prevent checkout', async ({ page }) => {
    await page.route('**/api/orders', async (route) => {
      await route.fulfill({
        status: 400,
        json: { success: false, message: 'Không đủ tồn kho' },
      });
    });

    await checkoutPage.fillShippingAddress('789 Pasteur, HCM');
    await checkoutPage.placeOrderBtn.click();

    await expect(checkoutPage.formMessage).toContainText('Không đủ tồn kho');
    await expect(checkoutPage.emptyCartMessage).toHaveCount(0);
    expect(removeCartItemCalls).toBe(0);
  });

  test('Payment method selection', async ({ page }) => {
    let orderPayload: { paymentMethod?: string } | null = null;

    await page.route('**/api/orders', async (route) => {
      orderPayload = route.request().postDataJSON();
      await route.fulfill({
        status: 201,
        json: { orderId: 'ORD-COD', status: 'PENDING', totalPrice: 15050000 },
      });
    });

    await checkoutPage.fillShippingAddress('123 Test St');
    await checkoutPage.placeOrder();

    expect(orderPayload).toMatchObject({ paymentMethod: 'COD' });
    await expect(checkoutPage.successMessage).toContainText('ORD-COD');
  });

  test('Place order sends discounted total and clears the cart', async ({ page }) => {
    let orderPayload: {
      couponCode?: string;
      shippingFee?: number;
      shippingAddress?: string;
      items?: Array<{ productId: string; quantity: number; price: number }>;
    } | null = null;

    await page.route('**/api/orders', async (route) => {
      orderPayload = route.request().postDataJSON();
      await route.fulfill({
        status: 201,
        json: { orderId: 'ORD-SALE10', status: 'PENDING', totalPrice: 13550000 },
      });
    });

    await checkoutPage.applyCoupon('SALE10');
    await checkoutPage.fillShippingAddress('456 Le Loi, HCM');
    await checkoutPage.placeOrder();

    expect(orderPayload).toMatchObject({
      couponCode: 'SALE10',
      shippingFee: 50000,
      shippingAddress: '456 Le Loi, HCM',
      items: [{ productId: 'P001', quantity: 1, price: 15000000 }],
    });
    await expect(checkoutPage.successMessage).toContainText('ORD-SALE10');
    await expect(checkoutPage.emptyCartMessage).toBeVisible();
  });

  test('Order API failure - keeps cart and shows error message', async ({ page }) => {
    await page.route('**/api/orders', async (route) => {
      await route.fulfill({
        status: 400,
        json: { success: false, message: 'Không đủ tồn kho' },
      });
    });

    await checkoutPage.fillShippingAddress('789 Pasteur, HCM');
    await checkoutPage.placeOrderBtn.click();

    await expect(checkoutPage.formMessage).toContainText('Không đủ tồn kho');
    await expect(checkoutPage.cartItem('Laptop Dell')).toBeVisible();
    expect(removeCartItemCalls).toBe(0);
  });

  test('Navigation returns from cart to product list', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Giỏ hàng' }).click();
    await expect(page).toHaveURL(/\/cart/);

    await page.getByRole('button', { name: 'San pham' }).click();

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByRole('heading', { name: 'Danh sách sản phẩm' })).toBeVisible();
  });

  test('Multiple coupon attempts', async () => {
    await checkoutPage.applyCoupon('SALE10');
    await expect(checkoutPage.discountAmount).toContainText('1.500.000');
    await expect(checkoutPage.checkoutTotal).toContainText('13.550.000');

    await checkoutPage.applyCoupon('FIXED100K');

    await expect(checkoutPage.formMessage).toContainText('FIXED100K');
    await expect(checkoutPage.discountAmount).toContainText('100.000');
    await expect(checkoutPage.checkoutTotal).toContainText('14.950.000');
  });
});

test.describe('Checkout E2E - Edge Cases', () => {
  test('Empty cart checkout attempt', async ({ page }) => {
    await page.route('**/api/cart/user01', async (route) => {
      await route.fulfill({
        json: {
          success: true,
          items: [],
          itemCount: 0,
          cartTotal: 0,
        },
      });
    });

    await page.goto('/cart');

    await expect(page.getByTestId('empty-cart-message')).toBeVisible();
    await expect(page.getByTestId('place-order-btn')).toHaveCount(0);
  });
});
