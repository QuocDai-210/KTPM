import { expect, type Locator, type Page } from '@playwright/test';

export default class CartPage {
  readonly page: Page;
  readonly cartButton: Locator;
  readonly cartBadge: Locator;
  readonly emptyCartMessage: Locator;
  readonly cartTotal: Locator;
  readonly cartSubtotal: Locator;
  readonly formMessage: Locator;
  readonly errorMessage: Locator;
  readonly appShell: Locator;

  constructor(page: Page) {
    this.page = page;
    this.cartButton = page.getByRole('button', { name: 'Giỏ hàng' });
    this.cartBadge = page.getByTestId('cart-badge');
    this.emptyCartMessage = page.getByTestId('empty-cart-message');
    this.cartTotal = page.getByTestId('cart-total');
    this.cartSubtotal = page.getByTestId('cart-subtotal');
    this.formMessage = page.getByTestId('form-message');
    this.errorMessage = page.getByTestId('error-message');
    this.appShell = page.locator('#app-shell');
  }

  productCard(productId: string) {
    return this.page.getByTestId(`product-${productId}`);
  }

  productStock(productId: string) {
    return this.page.getByTestId(`stock-${productId}`);
  }

  quantityInput(productId: string) {
    return this.page.getByTestId(`quantity-input-${productId}`);
  }

  addToCartButton(productId: string) {
    return this.page.getByTestId(`add-to-cart-${productId}`);
  }

  deleteProductButton(productId: string) {
    return this.page.getByTestId(`delete-product-${productId}`);
  }

  async goto() {
    await this.page.goto('/');
    await expect(this.appShell).toBeVisible();
  }

  async openCart() {
    await this.cartButton.click();
    await expect(this.page).toHaveURL(/\/cart$/);
  }

  async addProduct(productId: string, quantity: number) {
    await this.quantityInput(productId).fill(String(quantity));
    await this.addToCartButton(productId).click();
  }

  async updateCartQuantity(productId: string, quantity: number) {
    await this.quantityInput(productId).fill(String(quantity));
  }

  async removeProduct(productId: string) {
    await this.deleteProductButton(productId).click();
  }

  async expectProductVisible(productName: string) {
    await expect(this.page.getByText(productName)).toBeVisible();
  }

  async expectSuccessMessage(message: string) {
    await expect(this.page.getByText(message)).toBeVisible();
  }
}
