# Playwright E2E Testing Guide

## Getting Started

This project uses Playwright for end-to-end (e2e) testing.

### Install Dependencies

Playwright is already in `package.json`, but if you need to install it:

```bash
npm install
npx playwright install  # Install browser binaries
```

### Run Tests

- **Run all tests**: `npm run test:e2e`
- **Run tests with UI**: `npm run test:e2e:ui`
- **Debug tests**: `npm run test:e2e:debug`
- **View test report**: `npm run test:e2e:report`
- **Run specific test file**: `npx playwright test e2e/home.spec.ts`
- **Run tests in headed mode**: `npx playwright test --headed`

### Configuration

The configuration file is `playwright.config.ts` with:
- **Base URL**: `http://localhost:5173` (development server)
- **Browsers**: Chromium, Firefox, WebKit
- **Auto web server**: Starts dev server automatically during tests
- **Screenshots**: Captured only on failure
- **Tracing**: Enabled on first retry

### Writing Tests

Tests are located in the `e2e/` directory with `*.spec.ts` extension.

Example test:
```typescript
import { test, expect } from '@playwright/test';

test('should navigate to home page', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveTitle(/.*/, { timeout: 5000 });
});
```

### Test Structure

- `e2e/home.spec.ts` - Home page tests
- `e2e/navigation.spec.ts` - Navigation and responsiveness tests

### Best Practices

1. Use `test.describe()` to group related tests
2. Use `test.beforeEach()` for common setup
3. Use meaningful test descriptions
4. Keep tests independent and idempotent
5. Use locators like `page.locator()` instead of `page.querySelector()`

### CI/CD

Tests run in CI with:
- 2 retries on failure
- 1 worker (sequential)
- Headless mode
- HTML report generation

### Debugging

- Run `npm run test:e2e:debug` to step through tests
- Use `page.pause()` to pause execution in debug mode
- Check `playwright-report/` folder after failed tests
