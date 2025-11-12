import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { productService } from '../../services/productService';
import type { ProductDetailResponse } from '../../types/product';
import { ImageGallery } from '../../components/product/ImageGallery';
import { Breadcrumbs, Button, Badge, Skeleton, TextSkeleton } from '../../components/ui';
import { useCart } from '../../contexts/CartContext';
import { useToast } from '../../components/ui';

export default function ProductDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);
  const { addItem } = useCart();
  const { showToast } = useToast();

  useEffect(() => {
    if (id) {
      loadProduct();
    }
  }, [id]);

  const loadProduct = async () => {
    try {
      const data = await productService.getProductById(Number(id));
      setProduct(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Không tìm thấy sản phẩm');
      showToast('Không thể tải thông tin sản phẩm', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = () => {
    if (!product) return;
    
    for (let i = 0; i < quantity; i++) {
      addItem({
        productId: product.id,
        name: product.name,
        price: product.discountPrice || product.price,
        thumbnail: product.thumbnail,
      });
    }
    
    showToast(`Đã thêm ${quantity} sản phẩm vào giỏ hàng!`, 'success');
  };

  const handleBuyNow = () => {
    handleAddToCart();
    navigate('/cart');
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        <div className="mb-6">
          <Skeleton height={20} width={200} />
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12">
          <Skeleton className="aspect-square" />
          <div className="space-y-6">
            <div>
              <Skeleton height={32} className="w-3/4 mb-4" />
              <Skeleton height={48} className="w-1/2" />
            </div>
            <TextSkeleton lines={4} />
            <div className="flex gap-3">
              <Skeleton height={48} className="flex-1" />
              <Skeleton height={48} className="w-32" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center max-w-md mx-auto bg-white rounded-2xl p-12 border border-gray-200">
          <div className="mb-6">
            <svg 
              className="mx-auto h-20 w-20 text-gray-400" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
              aria-hidden="true"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Không tìm thấy sản phẩm</h2>
          <p className="text-gray-600 mb-6">{error || 'Sản phẩm không tồn tại hoặc đã bị xóa'}</p>
          <Button onClick={() => navigate('/products')}>
            Quay lại danh sách sản phẩm
          </Button>
        </div>
      </div>
    );
  }

  const hasDiscount = !!(product.discountPrice && product.discountPrice < product.price);
  const discountPercent = hasDiscount
    ? Math.round(((product.price - product.discountPrice!) / product.price) * 100)
    : 0;

  const allImages = product.images && product.images.length > 0 ? product.images : [product.thumbnail];

  const breadcrumbItems = [
    { label: 'Trang chủ', href: '/' },
    { label: 'Sản phẩm', href: '/products' },
    ...(product.category ? [{ label: product.category.name, href: `/products?category=${product.category.id}` }] : []),
    { label: product.name },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
      {/* Breadcrumb */}
      <div className="mb-6">
        <Breadcrumbs items={breadcrumbItems} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12">
        {/* Product Images with Gallery */}
        <ImageGallery
          images={allImages}
          productName={product.name}
          hasDiscount={hasDiscount}
          discountPercent={discountPercent}
        />

        {/* Product Info */}
        <div className="space-y-6">
          {/* Title and Category */}
          <div>
            {product.category && (
              <Badge variant="info" className="mb-3">
                <Link to={`/products?category=${product.category.id}`}>
                  {product.category.name}
                </Link>
              </Badge>
            )}
            <h1 className="text-2xl sm:text-3xl lg:text-4xl font-bold text-gray-900 leading-tight mb-4">
              {product.name}
            </h1>
          </div>

          {/* Price */}
          <div className="bg-gray-50 rounded-xl p-6 border border-gray-200">
            <div className="flex items-baseline gap-4 mb-2">
              {hasDiscount ? (
                <>
                  <span className="text-3xl sm:text-4xl font-bold text-red-600">
                    {new Intl.NumberFormat('vi-VN', {
                      style: 'currency',
                      currency: 'VND',
                    }).format(product.discountPrice!)}
                  </span>
                  <span className="text-xl text-gray-400 line-through">
                    {new Intl.NumberFormat('vi-VN', {
                      style: 'currency',
                      currency: 'VND',
                    }).format(product.price)}
                  </span>
                  <Badge variant="error" size="lg">
                    Giảm {discountPercent}%
                  </Badge>
                </>
              ) : (
                <span className="text-3xl sm:text-4xl font-bold text-blue-600">
                  {new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND',
                  }).format(product.price)}
                </span>
              )}
            </div>

            {/* Stock Status */}
            <div className="flex items-center gap-2 text-sm">
              {product.active ? (
                <>
                  <svg className="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                  <span className="font-medium text-green-700">Còn hàng</span>
                </>
              ) : (
                <>
                  <svg className="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                  <span className="font-medium text-red-700">Hết hàng</span>
                </>
              )}
            </div>
          </div>

          {/* Quantity Selector */}
          <div className="flex items-center gap-4">
            <label htmlFor="quantity" className="text-sm font-medium text-gray-700">
              Số lượng:
            </label>
            <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">
              <button
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                className="px-4 py-2 bg-gray-50 hover:bg-gray-100 text-gray-700 font-medium transition-colors"
                aria-label="Decrease quantity"
              >
                −
              </button>
              <input
                id="quantity"
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                className="w-16 px-4 py-2 text-center border-x border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
                aria-label="Quantity"
              />
              <button
                onClick={() => setQuantity((q) => q + 1)}
                className="px-4 py-2 bg-gray-50 hover:bg-gray-100 text-gray-700 font-medium transition-colors"
                aria-label="Increase quantity"
              >
                +
              </button>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row gap-3 pt-4">
            <Button
              onClick={handleAddToCart}
              disabled={!product.active}
              fullWidth
              size="lg"
            >
              <svg className="w-5 h-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              Thêm vào giỏ hàng
            </Button>
            <Button
              onClick={handleBuyNow}
              disabled={!product.active}
              variant="secondary"
              size="lg"
              className="sm:w-auto"
            >
              Mua ngay
            </Button>
          </div>

          {/* Description */}
          {product.description && (
            <div className="pt-6 border-t border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900 mb-3">Mô tả sản phẩm</h2>
              <div className="prose prose-sm max-w-none text-gray-700 leading-relaxed whitespace-pre-line">
                {product.description}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
