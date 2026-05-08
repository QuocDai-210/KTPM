import { test, expect } from '@playwright/test';

test.describe('Purchase E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/products', async (route) => {
      await route.fulfill({ json: [{ id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 }] });
    });
    await page.route('**/api/cart/user01', async (route) => {
      await route.fulfill({
        json: {
          success: true,
          items: [{ productId: 'P001', productName: 'Laptop Dell', quantity: 1, price: 15000000 }],
          itemCount: 1,
          cartTotal: 15000000,
        },
      });
    });
    await page.route('**/api/orders', async (route) => {
      await route.fulfill({ status: 201, json: { orderId: 'ORD-E2E', status: 'PENDING', totalPrice: 15050000 } });
    });
    await page.goto('/cart');
  });

  test('TC1: Đặt hàng thành công', async ({ page }) => {
    await expect(page.getByText('Laptop Dell')).toBeVisible();
    await page.getByTestId('shipping-address-input').fill('123 Nguyen Trai, HCM');
    await page.getByTestId('place-order-btn').click();
    await expect(page.getByTestId('order-success')).toContainText('ORD-E2E');
  });

  test('TC2: Kiểm tra tính toán giá chính xác', async ({ page }) => {
    await expect(page.getByTestId('cart-subtotal')).toContainText('15.000.000');
    await expect(page.getByTestId('shipping-fee')).toContainText('50.000');
    await expect(page.getByTestId('checkout-total')).toContainText('15.050.000');
  });

  test('TC3: Checkout flow hoàn chỉnh', async ({ page }) => {
    await page.getByTestId('coupon-input').fill('SALE10');
    await page.getByTestId('apply-coupon-btn').click();
    await expect(page.getByTestId('form-message')).toContainText('SALE10');
    await expect(page.getByTestId('checkout-total')).toContainText('13.550.000');
    await page.getByTestId('shipping-address-input').fill('456 Le Loi, HCM');
    await page.getByTestId('place-order-btn').click();
    await expect(page.getByTestId('order-success')).toContainText('Đặt hàng thành công');
  });
});
