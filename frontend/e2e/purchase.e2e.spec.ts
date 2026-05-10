import { expect, test } from '@playwright/test';
import CheckoutPage from './pages/CheckoutPage';

const cartItem = {
  productId: 'P001',
  productName: 'Laptop Dell',
  quantity: 1,
  price: 15000000,
};

test.describe('Purchase E2E Tests', () => {
  let checkoutPage: CheckoutPage;
  let removeCartItemCalls: number;

  test.beforeEach(async ({ page }) => {
    checkoutPage = new CheckoutPage(page);
    removeCartItemCalls = 0;

    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('userId', 'user01');
    });

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
        json: { orderId: 'ORD-E2E', status: 'PENDING', totalPrice: 15050000 },
      });
    });

    await page.goto('/');
    await checkoutPage.gotoCart();
  });

  test('TC1: Nên đặt hàng thành công và cập nhật giỏ hàng', async ({ page }) => {
    await expect(checkoutPage.cartItem('Laptop Dell')).toBeVisible();
    await checkoutPage.goToCheckout();
    await checkoutPage.fillShippingAddress('123 Nguyen Trai, HCM');
    await checkoutPage.placeOrder();

    await expect(checkoutPage.successMessage).toContainText('ORD-E2E');
    await expect(checkoutPage.emptyCartMessage).toBeVisible();
    await expect(page).toHaveURL(/\/cart/);
    expect(removeCartItemCalls).toBe(1);
  });

  test('TC2: Tính giá chính xác subtotal, discount, shipping và total', async () => {
    await expect(checkoutPage.cartSubtotal).toContainText('15.000.000');
    await expect(checkoutPage.discountAmount).toContainText('0');
    await expect(checkoutPage.shippingFee).toContainText('50.000');
    await expect(checkoutPage.checkoutTotal).toContainText('15.050.000');

    await checkoutPage.applyCoupon('SALE10');

    await expect(checkoutPage.cartSubtotal).toContainText('15.000.000');
    await expect(checkoutPage.discountAmount).toContainText('1.500.000');
    await expect(checkoutPage.shippingFee).toContainText('50.000');
    await expect(checkoutPage.checkoutTotal).toContainText('13.550.000');
  });

  test('TC3: Áp dụng mã giảm giá và cập nhật giỏ sau khi đặt hàng', async () => {
    await checkoutPage.applyCoupon('SALE10');
    await expect(checkoutPage.formMessage).toContainText('SALE10');
    await expect(checkoutPage.checkoutTotal).toContainText('13.550.000');
    await checkoutPage.fillShippingAddress('456 Le Loi, HCM');
    await checkoutPage.placeOrder();
    await expect(checkoutPage.successMessage).toContainText('Đặt hàng thành công');
    await expect(checkoutPage.emptyCartMessage).toBeVisible();
    expect(removeCartItemCalls).toBe(1);
  });
});
