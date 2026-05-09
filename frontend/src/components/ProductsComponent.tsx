import { useState, useEffect } from 'react';
import * as cartService from '../services/cartService';

interface ProductsComponentProps {
  userId: string;
  onAddToCart?: (productId: string, quantity: number) => void;
}

const fallbackProducts: cartService.Product[] = [
  { id: 'P001', name: 'Laptop Dell XPS 13', price: 15000000, stock: 10 },
  { id: 'P002', name: 'Wireless Mouse', price: 500000, stock: 50 },
  { id: 'P003', name: 'USB-C Cable', price: 150000, stock: 0 },
];

const ProductsComponent = ({
  userId,
  onAddToCart,
}: ProductsComponentProps) => {
  const [products, setProducts] = useState<cartService.Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [quantities, setQuantities] = useState<{ [key: string]: number }>({});

  useEffect(() => {
    let active = true;
    cartService
      .getProducts()
      .then((data) => {
        if (active) {
          setProducts(Array.isArray(data) ? data : fallbackProducts);
          setError(null);
        }
      })
      .catch(() => {
        if (active) {
          setProducts(fallbackProducts);
          setError('Đang dùng dữ liệu mẫu vì chưa kết nối được API sản phẩm');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [userId]);

  const handleQuantityChange = (productId: string, quantity: number) => {
    if (quantity < 0) return;
    setQuantities((prev) => ({
      ...prev,
      [productId]: quantity,
    }));
  };

  const handleAddToCart = (product: cartService.Product) => {
    const quantity = quantities[product.id] ?? 1;
    if (quantity < 1) {
      setError('Số lượng phải lớn hơn 0');
      return;
    }
    if (quantity > product.stock) {
      setError(`Chỉ còn ${product.stock} sản phẩm`);
      return;
    }
    onAddToCart?.(product.id, quantity);
    setQuantities((prev) => ({
      ...prev,
      [product.id]: 1,
    }));
  };

  if (loading) {
    return (
      <div data-testid="loading-spinner">
        <p>Đang tải sản phẩm...</p>
      </div>
    );
  }

  return (
    <div className="products-container">
      <div className="section-heading">
        <p>ShopCart</p>
        <h1>Danh sách sản phẩm</h1>
      </div>
      {error && <div data-testid="error-message" className="error">{error}</div>}
      
      <div className="products-grid">
        {products.map((product) => (
          <div key={product.id} data-testid={`product-${product.id}`} className="product-card">
            <h3>{product.name}</h3>
            <p className="price">
              {new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND',
              }).format(product.price)}
            </p>
            <p className="stock" data-testid={`stock-${product.id}`}>
              Tồn kho: {product.stock}
            </p>
            
            <div className="quantity-selector">
              <label htmlFor={`quantity-${product.id}`}>Số lượng</label>
              <input
                id={`quantity-${product.id}`}
                type="number"
                min="1"
                max={product.stock}
                value={quantities[product.id] ?? 1}
                onChange={(e) =>
                  handleQuantityChange(product.id, parseInt(e.target.value) || 0)
                }
                data-testid={`quantity-input-${product.id}`}
              />
            </div>

            <button
              onClick={() => handleAddToCart(product)}
              disabled={product.stock === 0}
              data-testid={`add-to-cart-${product.id}`}
              className="add-to-cart-btn"
            >
              {product.stock === 0 ? 'Hết hàng' : 'Thêm vào giỏ'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProductsComponent;
