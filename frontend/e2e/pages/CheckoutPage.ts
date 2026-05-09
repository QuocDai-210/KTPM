import { expect, type Locator, type Page } from '@playwright/test';

// Page Object Model for Checkout
export default class CheckoutPage {
  readonly page: Page;
  readonly checkoutBtn: Locator;
  readonly couponInput: Locator;
  readonly applyCouponBtn: Locator;
  readonly placeOrderBtn: Locator;
  readonly totalDisplay: Locator;
  readonly successMessage: Locator;
  readonly shippingAddressInput: Locator;
  readonly formMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.checkoutBtn = page.getByTestId('checkout-btn');
    this.couponInput = page.getByTestId('coupon-input');
    this.applyCouponBtn = page.getByTestId('apply-coupon-btn');
    this.placeOrderBtn = page.getByTestId('place-order-btn');
    this.totalDisplay = page.getByTestId('total-display');
    this.successMessage = page.getByTestId('order-success');
    this.shippingAddressInput = page.getByTestId('shipping-address-input');
    this.formMessage = page.getByTestId('form-message');
  }

  async goToCheckout() {
    await this.checkoutBtn.click();
    await expect(this.placeOrderBtn).toBeVisible();
  }

  async applyCoupon(code: string) {
    await this.couponInput.fill(code);
    await this.applyCouponBtn.click();
  }

  async placeOrder() {
    await this.placeOrderBtn.click();
    await this.successMessage.waitFor();
  }

  async getTotalPrice(): Promise<string> {
    return await this.totalDisplay.innerText();
  }
}
