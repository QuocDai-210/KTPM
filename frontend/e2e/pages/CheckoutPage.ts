import { expect, type Locator, type Page } from '@playwright/test';

export class CheckoutPage {
  readonly page: Page;
  readonly cartBadge: Locator;
  readonly checkoutBtn: Locator;
  readonly couponInput: Locator;
  readonly applyCouponBtn: Locator;
  readonly placeOrderBtn: Locator;
  readonly totalDisplay: Locator;
  readonly successMessage: Locator;
  readonly inventoryWarning: Locator;
  readonly cartSubtotal: Locator;
  readonly discountAmount: Locator;
  readonly shippingFee: Locator;
  readonly checkoutTotal: Locator;
  readonly shippingAddressInput: Locator;
  readonly formMessage: Locator;
  readonly emptyCartMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.cartBadge = page.locator('[data-testid="cart-badge"]');
    this.checkoutBtn = page.locator('[data-testid="checkout-btn"]');
    this.couponInput = page.locator('[data-testid="coupon-input"]');
    this.applyCouponBtn = page.locator('[data-testid="apply-coupon-btn"]');
    this.placeOrderBtn = page.locator('[data-testid="place-order-btn"]');
    this.totalDisplay = page.locator('[data-testid="total-display"]');
    this.successMessage = page.locator('[data-testid="order-success"]');
    this.inventoryWarning = page.locator('[data-testid="inventory-warning"]');
    this.cartSubtotal = page.locator('[data-testid="cart-subtotal"]');
    this.discountAmount = page.locator('[data-testid="discount-amount"]');
    this.shippingFee = page.locator('[data-testid="shipping-fee"]');
    this.checkoutTotal = page.locator('[data-testid="checkout-total"]');
    this.shippingAddressInput = page.locator('[data-testid="shipping-address-input"]');
    this.formMessage = page.locator('[data-testid="form-message"]');
    this.emptyCartMessage = page.locator('[data-testid="empty-cart-message"]');
  }

  async gotoCart() {
    await this.page.goto('/cart');
    await expect(this.placeOrderBtn).toBeVisible();
  }

  cartItem(productName: string) {
    return this.page.getByText(productName);
  }

  async goToCheckout() {
    await this.checkoutBtn.click();
    await expect(this.placeOrderBtn).toBeVisible();
  }

  async applyCoupon(code: string) {
    await this.couponInput.fill(code);
    await this.applyCouponBtn.click();
  }

  async fillShippingAddress(address: string) {
    await this.shippingAddressInput.fill(address);
  }

  async placeOrder() {
    await this.placeOrderBtn.click();
    await this.successMessage.waitFor();
  }

  async getTotalPrice(): Promise<string> {
    return await this.totalDisplay.innerText();
  }

  async expectCartItemVisible(productName: string) {
    await expect(this.cartItem(productName)).toBeVisible();
  }

  async expectPriceSummary(expected: {
    subtotal?: string;
    discount?: string;
    shipping?: string;
    total?: string;
  }) {
    if (expected.subtotal) {
      await expect(this.cartSubtotal).toContainText(expected.subtotal);
    }

    if (expected.discount) {
      await expect(this.discountAmount).toContainText(expected.discount);
    }

    if (expected.shipping) {
      await expect(this.shippingFee).toContainText(expected.shipping);
    }

    if (expected.total) {
      await expect(this.checkoutTotal).toContainText(expected.total);
    }
  }

  async expectFormMessage(message: string | RegExp) {
    await expect(this.formMessage).toContainText(message);
  }

  async expectOrderSuccess(message: string | RegExp) {
    await expect(this.successMessage).toContainText(message);
  }
}

export default CheckoutPage;
