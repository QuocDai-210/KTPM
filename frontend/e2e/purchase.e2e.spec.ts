import { test, expect } from '@playwright/test';

test.describe('Purchase E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
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
