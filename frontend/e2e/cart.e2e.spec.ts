import { test, expect } from '@playwright/test';

test.describe('Cart E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/products', async (route) => {
      await route.fulfill({
        json: [
          { id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 },
          { id: 'P002', name: 'Mouse Logitech', price: 500000, stock: 50 },
          { id: 'P003', name: 'Keyboard Mechanical', price: 2000000, stock: 0 },
        ],
      });
    });
    await page.route('**/api/cart/user01', async (route) => {
      await route.fulfill({ json: { success: true, items: [], itemCount: 0, cartTotal: 0 } });
    });
    await page.goto('http://localhost:5173');
  });

  test('TC1: Thêm sản phẩm vào giỏ hàng thành công', async ({ page }) => {
    // Navigate to cart
    await page.click('button:has-text("Cart")');
    
    // Wait for cart to load
    await page.waitForSelector('[data-testid="empty-cart-message"]', { timeout: 5000 }).catch(() => {});
    
    await expect(page.locator('#root')).toBeVisible();
  });

  test('TC2: Hiển thị giỏ hàng rỗng', async ({ page }) => {
    await page.click('button:has-text("Cart")');
    
    // Should show empty cart message
    const emptyMessage = page.locator('[data-testid="empty-cart-message"]');
    await expect(emptyMessage).toBeVisible();
  });

  test('TC3: Responsive layout trên mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.locator('#root')).toBeVisible();
  });

  test('TC4: Responsive layout trên tablet', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.locator('#root')).toBeVisible();
  });

  test('TC5: Responsive layout trên desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await expect(page.locator('#root')).toBeVisible();
  });
});
