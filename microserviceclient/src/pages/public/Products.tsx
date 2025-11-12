import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { productService } from '../../services/productService';
import type { ProductSummaryResponse } from '../../types/product';
import { ProductCard } from '../../components/product/ProductCard';
import { ProductCardSkeleton, Input, Badge, Button } from '../../components/ui';
import { useCart } from '../../contexts/CartContext';
import { useToast } from '../../components/ui';

export default function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductSummaryResponse[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [filtersOpen, setFiltersOpen] = useState(false);
  const { addItem } = useCart();
  const { showToast } = useToast();

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [page, searchParams]);

  const loadCategories = async () => {
    try {
      const data = await productService.getAllCategories();
      setCategories(data);
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    try {
      const categoryId = searchParams.get('category');
      const keyword = searchParams.get('search');
      
      let response;
      if (keyword) {
        response = await productService.searchProducts(keyword, page, 12);
      } else if (categoryId) {
        response = await productService.getProductsByCategory(Number(categoryId), page, 12);
      } else {
        response = await productService.getAllProducts(page, 12);
      }
      
      setProducts(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      console.error('Failed to load products:', error);
      showToast('Không thể tải sản phẩm. Vui lòng thử lại!', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (search.trim()) {
      setSearchParams({ search: search.trim() });
    } else {
      setSearchParams({});
    }
    setPage(0);
  };

  const handleAddToCart = (productId: number) => {
    const product = products.find((p) => p.id === productId);
    if (product) {
      addItem({
        productId: product.id,
        name: product.name,
        price: product.discountPrice || product.price,
        thumbnail: product.thumbnail,
      });
      showToast('Đã thêm vào giỏ hàng!', 'success');
    }
  };

  const clearFilters = () => {
    setSearchParams({});
    setSearch('');
    setPage(0);
  };

  const activeCategory = searchParams.get('category');
  const activeSearch = searchParams.get('search');
  const hasActiveFilters = activeCategory || activeSearch;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
      <div className="flex flex-col lg:flex-row gap-6 lg:gap-8">
        {/* Sidebar - Categories Filter */}
        <aside className="lg:w-64 flex-shrink-0">
          {/* Mobile Filter Toggle */}
          <button
            onClick={() => setFiltersOpen(!filtersOpen)}
            className="lg:hidden w-full mb-4 px-4 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-xl hover:bg-gray-50 transition-colors flex items-center justify-between"
          >
            <span>Bộ lọc</span>
            <svg 
              className={`w-5 h-5 transition-transform ${filtersOpen ? 'rotate-180' : ''}`}
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          <div className={`lg:block ${filtersOpen ? 'block' : 'hidden'} bg-white rounded-xl shadow-sm border border-gray-200 p-6 lg:sticky lg:top-24`}>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Danh mục</h3>
              {hasActiveFilters && (
                <button
                  onClick={clearFilters}
                  className="text-xs font-medium text-blue-600 hover:text-blue-700"
                >
                  Xóa bộ lọc
                </button>
              )}
            </div>
            <nav className="space-y-1">
              <Link
                to="/products"
                onClick={() => setFiltersOpen(false)}
                className={`
                  block px-4 py-2.5 text-sm font-medium rounded-lg transition-colors duration-200
                  ${
                    !activeCategory
                      ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600'
                      : 'text-gray-700 hover:bg-gray-50'
                  }
                `}
              >
                Tất cả
              </Link>
              {categories.map((category) => (
                <Link
                  key={category.id}
                  to={`/products?category=${category.id}`}
                  onClick={() => setFiltersOpen(false)}
                  className={`
                    block px-4 py-2.5 text-sm font-medium rounded-lg transition-colors duration-200
                    ${
                      activeCategory === String(category.id)
                        ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600'
                        : 'text-gray-700 hover:bg-gray-50'
                    }
                  `}
                >
                  {category.name}
                </Link>
              ))}
            </nav>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 min-w-0">
          {/* Search Bar and Active Filters */}
          <div className="mb-6 space-y-4">
            <form onSubmit={handleSearch} className="relative">
              <Input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm kiếm sản phẩm..."
                fullWidth
                startIcon={
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                }
              />
            </form>

            {/* Active Filters */}
            {hasActiveFilters && (
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm text-gray-600">Bộ lọc:</span>
                {activeSearch && (
                  <Badge variant="primary" className="flex items-center gap-1.5">
                    <span>Tìm: "{activeSearch}"</span>
                    <button
                      onClick={() => {
                        setSearch('');
                        const newParams = new URLSearchParams(searchParams);
                        newParams.delete('search');
                        setSearchParams(newParams);
                      }}
                      className="hover:text-blue-900"
                    >
                      <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </Badge>
                )}
                {activeCategory && (
                  <Badge variant="primary" className="flex items-center gap-1.5">
                    <span>
                      Danh mục: {categories.find(c => String(c.id) === activeCategory)?.name || activeCategory}
                    </span>
                    <button
                      onClick={() => {
                        const newParams = new URLSearchParams(searchParams);
                        newParams.delete('category');
                        setSearchParams(newParams);
                      }}
                      className="hover:text-blue-900"
                    >
                      <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </Badge>
                )}
                <button
                  onClick={clearFilters}
                  className="text-xs text-gray-500 hover:text-gray-700 underline"
                >
                  Xóa tất cả
                </button>
              </div>
            )}
          </div>

          {/* Products Grid */}
          {loading ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
              {[...Array(12)].map((_, i) => (
                <ProductCardSkeleton key={i} />
              ))}
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-16 bg-white rounded-xl border border-gray-200">
              <svg className="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-gray-500 text-lg font-medium">Không tìm thấy sản phẩm nào</p>
              <p className="text-gray-400 text-sm mt-1">Thử tìm kiếm với từ khóa khác hoặc thay đổi bộ lọc</p>
              {hasActiveFilters && (
                <Button
                  onClick={clearFilters}
                  variant="secondary"
                  size="sm"
                  className="mt-4"
                >
                  Xóa bộ lọc
                </Button>
              )}
            </div>
          ) : (
            <>
              <div className="mb-4 text-sm text-gray-600">
                Hiển thị <span className="font-semibold">{products.length}</span> sản phẩm
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6 mb-8">
                {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onAddToCart={handleAddToCart}
                    showAddToCart
                  />
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2">
                  <Button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    variant="secondary"
                    size="sm"
                  >
                    <svg className="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                    </svg>
                    Trước
                  </Button>

                  <div className="flex items-center gap-2">
                    {[...Array(Math.min(5, totalPages))].map((_, i) => {
                      let pageNum: number;
                      if (totalPages <= 5) {
                        pageNum = i;
                      } else if (page < 3) {
                        pageNum = i;
                      } else if (page > totalPages - 4) {
                        pageNum = totalPages - 5 + i;
                      } else {
                        pageNum = page - 2 + i;
                      }

                      return (
                        <button
                          key={i}
                          onClick={() => setPage(pageNum)}
                          className={`
                            px-3 py-1.5 text-sm font-medium rounded-lg transition-colors
                            ${
                              page === pageNum
                                ? 'bg-blue-600 text-white'
                                : 'text-gray-700 hover:bg-gray-100'
                            }
                          `}
                        >
                          {pageNum + 1}
                        </button>
                      );
                    })}
                  </div>

                  <Button
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    variant="secondary"
                    size="sm"
                  >
                    Sau
                    <svg className="w-4 h-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </Button>
                </div>
              )}
            </>
          )}
        </main>
      </div>
    </div>
  );
}
