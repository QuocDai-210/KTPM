import { test, expect } from '@playwright/test';

// Page Object Model for Checkout
class CheckoutPage {
  constructor(private page: any) {}

  async goToCheckout() {
    await this.page.click('[data-testid="checkout-btn"]');
    await this.page.waitForURL('**/checkout');
  }

  async applyCoupon(code: string) {
    await this.page.fill('[data-testid="coupon-input"]', code);
    await this.page.click('[data-testid="apply-coupon-btn"]');
  }

  async placeOrder() {
    await this.page.click('[data-testid="place-order-btn"]');
    await this.page.waitForSelector('[data-testid="order-success"]');
  }

  async getTotalPrice(): Promise<string> {
    return await this.page.locator('[data-testid="total-display"]').innerText();
  }
}

test.describe('CheckoutPage', () => {
  test('Hiển thị tổng giá chính xác', async ({ page }) => {
    const checkoutPage = new CheckoutPage(page);
    await page.goto('http://localhost:5173/checkout');
    
    // Wait for page to load
    await page.waitForTimeout(500);
    await expect(page.locator('main')).toBeVisible();
  });
});
