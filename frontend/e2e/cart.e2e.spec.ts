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
          items: cartItems,
        },
      });
    });

    await page.route('**/api/cart/user01', async (route) => {
      await route.fulfill({
        json: {
          success: true,
          items: cartItems,
          itemCount: cartItems.length ? 2 : 0,
          cartTotal: cartItems.length ? 30000000 : 0,
        },
      });
    });

    await page.goto('/');
  });

  test('TC1: Thêm sản phẩm vào giỏ hàng thành công', async ({ page }) => {
    await page.getByTestId('quantity-input-P001').fill('2');
    await page.getByTestId('add-to-cart-P001').click();

    await expect(page.getByTestId('cart-badge')).toHaveText('2');
    await expect(page.getByText('Them vao gio hang thanh cong')).toBeVisible();
    await expect(page.getByText('Laptop Dell')).toBeVisible();
    await expect(page.getByTestId('cart-total')).toContainText('30.000.000');
  });

  test('TC2: Hiển thị validation khi số lượng vượt tồn kho', async ({ page }) => {
    await page.getByTestId('quantity-input-P001').fill('50');
    await page.getByTestId('add-to-cart-P001').click();

    await expect(page.getByTestId('error-message')).toContainText('Chỉ còn 10 sản phẩm');
  });

  test('TC3: Hiển thị giỏ hàng rỗng', async ({ page }) => {
    await page.getByRole('button', { name: 'Giỏ hàng' }).click();

    await expect(page.getByTestId('empty-cart-message')).toBeVisible();
  });

  test('TC4: Khóa thao tác thêm sản phẩm hết hàng', async ({ page }) => {
    await expect(page.getByTestId('stock-P003')).toContainText('Tồn kho: 0');
    await expect(page.getByTestId('add-to-cart-P003')).toBeDisabled();
  });
});
