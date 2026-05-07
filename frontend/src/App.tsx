import { useState } from 'react'
import CartComponent from './components/CartComponent'
import './App.css'

function App() {
  const [page, setPage] = useState<'cart' | 'products'>('products')
  const userId = 'user01'

  return (
    <div id="root">
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
          <div>
            <h1>Products</h1>
            <p>Product listing would go here (P001: Laptop Dell 15M, P002: Mouse 500K, etc.)</p>
          </div>
        )}
        {page === 'cart' && <CartComponent userId={userId} />}
      </main>
    </div>
  )
}

export default App
