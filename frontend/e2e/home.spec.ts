import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test('should load the home page', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/.*/, { timeout: 5000 });
  });

  test('should display app container', async ({ page }) => {
    await page.goto('/');
    const appElement = await page.locator('#root');
    await expect(appElement).toBeVisible();
  });
});
