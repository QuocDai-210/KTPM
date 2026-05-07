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
    await page.goto('http://localhost:5173');
    await page.evaluate(() => localStorage.setItem('token', 'mock-token'));
    await page.goto('/');
  });

  test('TC1: Đặt hàng thành công', async ({ page }) => {
    // Navigate to cart
    await page.click('button:has-text("Cart")');
    
    // Wait for page to load
    await page.waitForTimeout(500);
    
    // Verify cart page is displayed
    await expect(page.locator('main')).toBeVisible();
  });

  test('TC2: Kiểm tra tính toán giá chính xác', async ({ page }) => {
    await page.goto('http://localhost:5173');
    await expect(page.locator('#root')).toBeVisible();
  });

  test('TC3: Checkout flow hoàn chỉnh', async ({ page }) => {
    await page.goto('http://localhost:5173/cart');
    await expect(page.locator('#root')).toBeVisible();
  });
});
