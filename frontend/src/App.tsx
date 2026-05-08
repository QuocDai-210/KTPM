import { useEffect, useState } from 'react'
import CartComponent from './components/CartComponent'
import ProductsComponent from './components/ProductsComponent'
import * as cartService from './services/cartService'
import './App.css'

function App() {
  const pathPage = window.location.pathname.includes('cart') || window.location.pathname.includes('checkout') ? 'cart' : 'products'
  const [page, setPage] = useState<'cart' | 'products'>(pathPage)
  const [notice, setNotice] = useState<string | null>(null)
  const userId = 'user01'

  useEffect(() => {
    window.history.replaceState(null, '', page === 'cart' ? '/cart' : '/')
  }, [page])

  const handleAddToCart = async (productId: string, quantity: number) => {
    try {
      await cartService.addToCart(userId, productId, quantity)
      setNotice('Thêm vào giỏ hàng thành công')
      setPage('cart')
    } catch (error) {
      setNotice('Không thể thêm sản phẩm vào giỏ')
      console.error('Error adding to cart:', error)
    }
  }

  return (
    <div id="app-shell">
      <nav className="topbar" aria-label="ShopCart navigation">
        <button className={page === 'products' ? 'active' : ''} onClick={() => setPage('products')}>
          Sản phẩm
        </button>
        <button className={page === 'cart' ? 'active' : ''} onClick={() => setPage('cart')}>
          Giỏ hàng
        </button>
      </nav>

      <main className="app-main">
        {notice && <div className="notice">{notice}</div>}
        {page === 'products' && (
          <ProductsComponent userId={userId} onAddToCart={handleAddToCart} />
        )}
        {page === 'cart' && <CartComponent userId={userId} />}
      </main>
    </div>
  )
}

export default App
