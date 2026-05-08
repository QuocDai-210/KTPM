import { test, expect } from '@playwright/test';

const products = [
  { id: 'P001', name: 'Laptop Dell', price: 15000000, stock: 10 },
  { id: 'P002', name: 'Mouse Logitech', price: 500000, stock: 50 },
  { id: 'P003', name: 'Keyboard Mechanical', price: 2000000, stock: 0 },
];

test.describe('Cart E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    let cartItems: Array<{ productId: string; productName: string; quantity: number; price: number }> = [];
    await page.route('**/api/products', async (route) => {
      await route.fulfill({ json: products });
    });
    await page.route('**/api/cart/add', async (route) => {
      cartItems = [{ productId: 'P001', productName: 'Laptop Dell', quantity: 2, price: 15000000 }];
      await route.fulfill({
        json: {
          success: true,
          message: 'Thêm vào giỏ hàng thành công',
          itemCount: 2,
          cartTotal: 30000000,
          items: [{ productId: 'P001', productName: 'Laptop Dell', quantity: 2, price: 15000000 }],
        },
      });
    });
    await page.route('**/api/cart/user01', async (route) => {
      await route.fulfill({
        json: cartItems.length
          ? {
              success: true,
              items: cartItems,
              itemCount: 2,
              cartTotal: 30000000,
            }
          : { success: true, items: [], itemCount: 0, cartTotal: 0 },
      });
    });
    await page.goto('/');
  });

  test('TC1: Thêm sản phẩm vào giỏ hàng thành công', async ({ page }) => {
    await expect(page.getByText('Laptop Dell')).toBeVisible();
    await page.getByTestId('quantity-input-P001').fill('2');
    await page.getByTestId('add-to-cart-P001').click();

    await expect(page.getByTestId('cart-badge')).toHaveText('2');
    await expect(page.getByText('Thêm vào giỏ hàng thành công')).toBeVisible();
    await expect(page.getByText('Laptop Dell')).toBeVisible();
    await expect(page.getByTestId('cart-total')).toContainText('30.000.000');
  });

  test('TC2: Hiển thị giỏ hàng rỗng', async ({ page }) => {
    await page.getByRole('button', { name: 'Giỏ hàng' }).click();
    await expect(page.getByTestId('empty-cart-message')).toBeVisible();
  });

  test('TC3: Chặn số lượng vượt tồn kho', async ({ page }) => {
    await page.getByTestId('quantity-input-P001').fill('50');
    await page.getByTestId('add-to-cart-P001').click();
    await expect(page.getByTestId('error-message')).toContainText('Chỉ còn 10 sản phẩm');
  });

  test('TC4: Responsive layout trên mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.locator('#app-shell')).toBeVisible();
  });

  test('TC5: Responsive layout trên tablet', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.locator('#app-shell')).toBeVisible();
  });

  test('TC6: Responsive layout trên desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await expect(page.locator('#app-shell')).toBeVisible();
  });
});
