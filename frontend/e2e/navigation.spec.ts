import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should have accessible UI elements', async ({ page }) => {
    // Check if the main app is rendered
    const root = page.locator('#root');
    await expect(root).toBeVisible();
  });

  test('page should be responsive', async ({ page }) => {
    // Mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    const root = page.locator('#root');
    await expect(root).toBeVisible();

    // Tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(root).toBeVisible();

    // Desktop viewport
    await page.setViewportSize({ width: 1920, height: 1080 });
    await expect(root).toBeVisible();
  });
});
