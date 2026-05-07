// Frontend cart validation utility
export interface CartItem {
  productId: string;
  quantity: number;
  stock: number;
}

export interface ValidationResult {
  valid: boolean;
  error?: string;
}

export function validateCartItem(item: CartItem): ValidationResult {
  // Validate product ID
  if (!item.productId || item.productId.trim() === '') {
    return { valid: false, error: 'Product ID không được để trống' };
  }

  // Validate quantity is not null/undefined
  if (item.quantity === null || item.quantity === undefined) {
    return { valid: false, error: 'Số lượng không được để trống' };
  }

  // Validate quantity is positive
  if (item.quantity <= 0) {
    return { valid: false, error: 'Số lượng phải lớn hơn 0' };
  }

  // Validate quantity is integer
  if (!Number.isInteger(item.quantity)) {
    return { valid: false, error: 'Số lượng phải là số nguyên' };
  }

  // Validate quantity doesn't exceed stock
  if (item.quantity > item.stock) {
    return { valid: false, error: `Số lượng vượt quá tồn kho. Tồn kho hiện tại: ${item.stock}` };
  }

  return { valid: true };
}

export interface CartTotal {
  items: Array<{ price: number; quantity: number }>;
  subtotal: number;
  discount: number;
  shipping: number;
  total: number;
}

export function calculateCartTotal(
  items: Array<{ price: number; quantity: number }>,
  discountPercent?: number,
  discountFixed?: number,
  shipping: number = 0
): CartTotal {
  // Calculate subtotal
  const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0);

  // Calculate discount
  let discount = 0;
  if (discountPercent) {
    discount = subtotal * (discountPercent / 100);
  } else if (discountFixed) {
    discount = discountFixed;
  }

  // Ensure discount doesn't exceed subtotal
  discount = Math.min(discount, subtotal);

  // Calculate total
  const total = subtotal - discount + shipping;

  return {
    items,
    subtotal,
    discount,
    shipping,
    total,
  };
}

export function checkInventoryAvailability(
  items: CartItem[]
): { available: boolean; message?: string } {
  for (const item of items) {
    if (item.quantity > item.stock) {
      return {
        available: false,
        message: `${item.productId}: Số lượng vượt quá tồn kho`,
      };
    }
  }
  return { available: true };
}
