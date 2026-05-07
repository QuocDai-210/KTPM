import { useState } from 'react'
import CartComponent from './components/CartComponent'
import ProductsComponent from './components/ProductsComponent'
import './App.css'

function App() {
  const [page, setPage] = useState<'cart' | 'products'>('products')
  const userId = 'user01'

  const handleAddToCart = async (productId: string, quantity: number) => {
    try {
      const response = await fetch('/api/cart/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer token123',
        },
        body: JSON.stringify({
          userId,
          productId,
          quantity,
        }),
      });
      if (response.ok) {
        alert('Thêm vào giỏ hàng thành công');
        setPage('cart');
      }
    } catch (error) {
      console.error('Error adding to cart:', error);
    }
  }

  return (
    <div id="app-shell">
      <nav style={{ padding: '1rem', borderBottom: '1px solid #ccc' }}>
        <button onClick={() => setPage('products')} style={{ marginRight: '1rem' }}>
          Products
        </button>
        <button onClick={() => setPage('cart')}>
          Cart
        </button>
      </nav>

      <main style={{ padding: '2rem' }}>
        {page === 'products' && (
          <ProductsComponent userId={userId} onAddToCart={handleAddToCart} />
        )}
        {page === 'cart' && <CartComponent userId={userId} />}
      </main>
    </div>
  )
}

export default App
