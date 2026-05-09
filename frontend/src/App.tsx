import { useEffect, useState } from 'react';
import CartComponent from './components/CartComponent';
import ProductsComponent from './components/ProductsComponent';
import * as cartService from './services/cartService';
import './App.css';

function App() {
  const pathPage =
    window.location.pathname.includes('cart') || window.location.pathname.includes('checkout')
      ? 'cart'
      : 'products';
  const [page, setPage] = useState<'cart' | 'products'>(pathPage);
  const [notice, setNotice] = useState<string | null>(null);
  const [cartCount, setCartCount] = useState(0);
  const userId = 'user01';

  useEffect(() => {
    window.history.replaceState(null, '', page === 'cart' ? '/cart' : '/');
  }, [page]);

  useEffect(() => {
    let active = true;
    cartService
      .getCart(userId)
      .then((response) => {
        if (!active) {
          return;
        }
        const items = Array.isArray(response?.items) ? response.items : [];
        setCartCount(items.reduce((sum, item) => sum + item.quantity, 0));
      })
      .catch(() => {
        if (active) {
          setCartCount(0);
        }
      });

    return () => {
      active = false;
    };
  }, [userId]);

  const handleAddToCart = async (productId: string, quantity: number) => {
    try {
      const response = await cartService.addToCart(userId, productId, quantity);
      const nextCount = response?.cartCount ?? response?.itemCount ?? quantity;
      setCartCount(nextCount);
      setNotice('Them vao gio hang thanh cong');
      setPage('cart');
    } catch (error) {
      setNotice('Không thể thêm sản phẩm vào giỏ');
      console.error('Error adding to cart:', error);
    }
  };

  return (
    <div id="app-shell">
      <nav className="topbar" aria-label="ShopCart navigation">
        <button
          aria-label="San pham"
          className={page === 'products' ? 'active' : ''}
          onClick={() => setPage('products')}
        >
          Sản phẩm
        </button>
        <button
          aria-label="Giỏ hàng"
          className={page === 'cart' ? 'active' : ''}
          onClick={() => setPage('cart')}
        >
          Giỏ hàng
          {cartCount > 0 && (
            <span aria-hidden="true" data-testid="cart-badge" className="cart-badge">
              {cartCount}
            </span>
          )}
        </button>
      </nav>

      <main className="app-main">
        {notice && <div className="notice">{notice}</div>}
        {page === 'products' && <ProductsComponent userId={userId} onAddToCart={handleAddToCart} />}
        {page === 'cart' && <CartComponent userId={userId} onCartCountChange={setCartCount} />}
      </main>
    </div>
  );
}

export default App;
