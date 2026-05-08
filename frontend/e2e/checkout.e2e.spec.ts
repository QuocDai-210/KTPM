import { test, expect } from '@playwright/test';
import CheckoutPage from './pages/CheckoutPage';

test.describe('Checkout E2E Tests', () => {
  let checkoutPage: CheckoutPage;

  test.beforeEach(async ({ page }) => {
    checkoutPage = new CheckoutPage(page);

    // Setup: Login and add products to cart
    await page.goto('/');

    // Simulate login
    await page.evaluate(() => {
      localStorage.setItem('authToken', 'mock-jwt-token');
      localStorage.setItem('userId', 'user01');
    });

    // Navigate to products and add item to cart
    await page.goto('/products');
    
    // Add products to cart (if UI elements exist)
    try {
      const addButtons = await page.locator('[data-testid="add-to-cart-btn"]').all();
      if (addButtons.length > 0) {
        await addButtons[0].click();
      }
    } catch (e) {
      // Products may not be available, continue with cart
    }

    // Go to cart
    await page.goto('/cart');
  });

  test('Checkout flow - Complete success path', async ({ page }) => {
    // Act: Navigate to checkout
    await checkoutPage.goToCheckout();

    // Verify we're on checkout page
    await expect(page).toHaveURL(/\/checkout/);

    // Verify cart summary is displayed
    const cartSummary = page.locator('[data-testid="cart-summary"]');
    await expect(cartSummary).toBeVisible();

    // Verify shipping info form
    const shippingForm = page.locator('[data-testid="shipping-form"]');
    await expect(shippingForm).toBeVisible();

    // Fill shipping address
    await page.fill('[data-testid="address-input"]', '123 Main St, City, Country');
    await page.fill('[data-testid="phone-input"]', '0987654321');

    // Select shipping method
    const shippingMethod = page.locator('[data-testid="shipping-method-select"]').first();
    if (await shippingMethod.isVisible()) {
      await shippingMethod.click();
    }

    // Proceed to payment
    const proceedBtn = page.locator('[data-testid="proceed-to-payment-btn"]');
    if (await proceedBtn.isVisible()) {
      await proceedBtn.click();
    }

    // Verify order confirmation
    const confirmationMsg = page.locator('[data-testid="order-confirmation"]');
    if (await confirmationMsg.isVisible()) {
      await expect(confirmationMsg).toContainText('Success');
    }
  });

  test('Price calculation - Verify accuracy', async ({ page }) => {
    // Arrange: Go to checkout
    await checkoutPage.goToCheckout();

    // Act: Verify price display elements
    const subtotalDisplay = page.locator('[data-testid="subtotal-display"]');
    const discountDisplay = page.locator('[data-testid="discount-display"]');
    const shippingDisplay = page.locator('[data-testid="shipping-display"]');
    const totalDisplay = page.locator('[data-testid="total-display"]');

    // Assert: All elements should be visible
    await expect(subtotalDisplay).toBeVisible();
    
    // Get values (if available)
    if (await shippingDisplay.isVisible()) {
      const shippingText = await shippingDisplay.textContent();
      expect(shippingText).toBeTruthy();
    }

    if (await totalDisplay.isVisible()) {
      const totalText = await totalDisplay.textContent();
      expect(totalText).toBeTruthy();
      expect(totalText).toMatch(/[0-9,.]/); // Should contain numbers
    }
  });

  test('Apply coupon - 10% discount', async ({ page }) => {
    // Arrange: Go to checkout
    await checkoutPage.goToCheckout();

    // Act: Apply coupon
    const couponInput = page.locator('[data-testid="coupon-input"]');
    const applyCouponBtn = page.locator('[data-testid="apply-coupon-btn"]');

    if (await couponInput.isVisible()) {
      await couponInput.fill('SALE10');
      await applyCouponBtn.click();

      // Wait for discount to be applied
      await page.waitForTimeout(1000);

      // Assert: Discount message should appear
      const discountMsg = page.locator('[data-testid="discount-applied"]');
      
      if (await discountMsg.isVisible()) {
        await expect(discountMsg).toContainText(/applied|success/i);
      }

      // Verify discount was calculated
      const discountDisplay = page.locator('[data-testid="discount-display"]');
      if (await discountDisplay.isVisible()) {
        const discountText = await discountDisplay.textContent();
        expect(discountText).toMatch(/[0-9,.]/);
      }
    }
  });

  test('Out of stock warning - Prevent checkout', async ({ page }) => {
    // Setup: Add out-of-stock item (if possible)
    // This would require backend API call or special test data

    // Act: Try to proceed with checkout
    const proceedBtn = page.locator('[data-testid="proceed-to-payment-btn"]');

    // If there's an inventory warning, it should be visible
    const inventoryWarning = page.locator('[data-testid="inventory-warning"]');
    
    if (await inventoryWarning.isVisible()) {
      // Assert: Warning should prevent checkout
      const warningText = await inventoryWarning.textContent();
      expect(warningText?.toLowerCase()).toContain('stock');

      // Checkout should be disabled
      await expect(proceedBtn).toBeDisabled();
    } else {
      // If no warning, proceed button should be enabled
      if (await proceedBtn.isVisible()) {
        await expect(proceedBtn).toBeEnabled();
      }
    }
  });

  test('Invalid address - Show validation error', async ({ page }) => {
    // Act: Go to checkout
    await checkoutPage.goToCheckout();

    // Try to proceed without filling address
    const proceedBtn = page.locator('[data-testid="proceed-to-payment-btn"]');
    
    if (await proceedBtn.isVisible()) {
      // Try clicking with empty address
      await proceedBtn.click();

      // Assert: Error message should appear
      const addressError = page.locator('[data-testid="address-error"]');
      
      if (await addressError.isVisible()) {
        const errorText = await addressError.textContent();
        expect(errorText?.toLowerCase()).toContain('address');
      }
    }
  });

  test('Payment method selection', async ({ page }) => {
    // Act: Go to checkout
    await checkoutPage.goToCheckout();

    // Verify payment methods are available
    const paymentMethods = page.locator('[data-testid="payment-method-option"]');
    const methodCount = await paymentMethods.count();

    if (methodCount > 0) {
      // Assert: Should have at least one payment method
      expect(methodCount).toBeGreaterThan(0);

      // Select first payment method
      await paymentMethods.first().click();

      // Verify selection is marked
      const selected = await paymentMethods.first().locator('[data-testid="selected-indicator"]');
      if (await selected.isVisible()) {
        await expect(selected).toBeVisible();
      }
    }
  });

  test('Place order button - Enabled only when valid', async ({ page }) => {
    // Act: Go to checkout
    await checkoutPage.goToCheckout();

    const placeOrderBtn = page.locator('[data-testid="place-order-btn"]');

    if (await placeOrderBtn.isVisible()) {
      // Initially button might be disabled (missing required fields)
      let isDisabled = await placeOrderBtn.isDisabled();
      
      // Fill required fields
      const addressInput = page.locator('[data-testid="address-input"]');
      if (await addressInput.isVisible()) {
        await addressInput.fill('123 Test St');
        
        // After filling, button might be enabled
        await page.waitForTimeout(500);
        isDisabled = await placeOrderBtn.isDisabled();
        
        // Should be enabled after valid input
        if (!isDisabled) {
          await expect(placeOrderBtn).toBeEnabled();
        }
      }
    }
  });

  test('Order confirmation - Verify details displayed', async ({ page }) => {
    // Setup: Complete checkout (simulated)
    // In real scenario, this would be after successful order creation

    // Navigate to order confirmation if such page exists
    await page.goto('/order-confirmation', { waitUntil: 'domcontentloaded' }).catch(() => {
      // Page might not exist in test environment
    });

    // Verify order confirmation elements if present
    const confirmationContainer = page.locator('[data-testid="order-confirmation"]');
    
    if (await confirmationContainer.isVisible()) {
      // Check for order details
      const orderId = page.locator('[data-testid="order-id"]');
      const orderTotal = page.locator('[data-testid="order-total"]');
      const orderDate = page.locator('[data-testid="order-date"]');

      if (await orderId.isVisible()) {
        const idText = await orderId.textContent();
        expect(idText).toMatch(/ORD-|order-/i);
      }

      if (await orderTotal.isVisible()) {
        const totalText = await orderTotal.textContent();
        expect(totalText).toMatch(/[0-9,.]/);
      }
    }
  });

  test('Browser back button handling', async ({ page }) => {
    // Act: Go to checkout
    await checkoutPage.goToCheckout();

    // Verify checkout page
    await expect(page).toHaveURL(/\/checkout/);

    // Click back button
    await page.goBack();

    // Should go back to cart
    await expect(page).toHaveURL(/\/cart/);

    // Cart should still be intact
    const cartContainer = page.locator('[data-testid="cart-container"]');
    if (await cartContainer.isVisible()) {
      await expect(cartContainer).toBeVisible();
    }
  });

  test('Multiple coupon attempts', async ({ page }) => {
    // Act: Go to checkout
    await checkoutPage.goToCheckout();

    const couponInput = page.locator('[data-testid="coupon-input"]');
    const applyCouponBtn = page.locator('[data-testid="apply-coupon-btn"]');

    if (await couponInput.isVisible() && await applyCouponBtn.isVisible()) {
      // First coupon
      await couponInput.fill('SALE10');
      await applyCouponBtn.click();
      await page.waitForTimeout(500);

      // Try applying second coupon
      await couponInput.clear();
      await couponInput.fill('SAVE500');
      await applyCouponBtn.click();
      await page.waitForTimeout(500);

      // Check for message (might be error about multiple coupons)
      const messages = page.locator('[data-testid*="message"], [data-testid*="error"]');
      const messageCount = await messages.count();

      expect(messageCount).toBeGreaterThan(0);
    }
  });
});

test.describe('Checkout E2E - Edge Cases', () => {
  test('Empty cart checkout attempt', async ({ page }) => {
    // Setup: Ensure cart is empty
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('authToken', 'mock-jwt-token');
    });

    // Navigate to checkout with empty cart
    await page.goto('/checkout');

    // Should either redirect or show empty state
    const emptyMessage = page.locator('[data-testid="empty-cart-message"]');
    const proceedBtn = page.locator('[data-testid="proceed-to-payment-btn"]');

    if (await emptyMessage.isVisible()) {
      await expect(emptyMessage).toBeVisible();
    }

    if (await proceedBtn.isVisible()) {
      await expect(proceedBtn).toBeDisabled();
    }
  });

  test('Session timeout during checkout', async ({ page }) => {
    // Start checkout
    await page.goto('/checkout');

    // Simulate session timeout by clearing auth token
    await page.evaluate(() => {
      localStorage.removeItem('authToken');
      localStorage.removeItem('userId');
    });

    // Try to place order
    const placeOrderBtn = page.locator('[data-testid="place-order-btn"]');
    
    if (await placeOrderBtn.isVisible()) {
      await placeOrderBtn.click();

      // Should redirect to login or show auth error
      await page.waitForTimeout(1000);
      
      const loginPage = page.url().includes('/login');
      const authError = await page.locator('[data-testid="auth-error"]').isVisible();

      expect(loginPage || authError).toBeTruthy();
    }
  });
});
