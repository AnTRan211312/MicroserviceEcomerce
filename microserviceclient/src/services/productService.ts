import { apiClient } from '../lib/axios';
import type {
  ApiResponse,
  PageResponse,
  ProductSummaryResponse,
  ProductDetailResponse,
  CategoryInfo,
} from '../types/product';

export const productService = {
  /**
   * Lấy danh sách sản phẩm với pagination và filtering
   */
  async getAllProducts(
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc',
    filters?: Record<string, any>
  ): Promise<PageResponse<ProductSummaryResponse>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort,
    });

    // Thêm filters nếu có
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value.toString());
        }
      });
    }

    const response = await apiClient.get<ApiResponse<PageResponse<ProductSummaryResponse>>>(
      `/api/products?${params.toString()}`
    );
    // Handle ApiResponse wrapper
    const responseData = response.data?.data || response.data;
    if (!responseData || !responseData.content) {
      throw new Error('Invalid response structure from API');
    }
    return responseData;
  },

  /**
   * Tìm kiếm sản phẩm
   */
  async searchProducts(
    keyword: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<ProductSummaryResponse>> {
    const response = await apiClient.get<ApiResponse<PageResponse<ProductSummaryResponse>>>(
      `/api/products/search`,
      {
        params: { keyword, page, size },
      }
    );
    // Handle ApiResponse wrapper
    return response.data?.data || response.data;
  },

  /**
   * Lấy sản phẩm nổi bật
   */
  async getFeaturedProducts(
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<ProductSummaryResponse>> {
    const response = await apiClient.get<ApiResponse<PageResponse<ProductSummaryResponse>>>(
      `/api/products/featured`,
      {
        params: { page, size },
      }
    );
    // Handle ApiResponse wrapper
    return response.data?.data || response.data;
  },

  /**
   * Lấy sản phẩm theo category
   */
  async getProductsByCategory(
    categoryId: number,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<ProductSummaryResponse>> {
    const response = await apiClient.get<ApiResponse<PageResponse<ProductSummaryResponse>>>(
      `/api/products/category/${categoryId}`,
      {
        params: { page, size },
      }
    );
    // Handle ApiResponse wrapper
    return response.data?.data || response.data;
  },

  /**
   * Lấy chi tiết sản phẩm theo ID
   */
  async getProductById(id: number): Promise<ProductDetailResponse> {
    const response = await apiClient.get<ApiResponse<ProductDetailResponse>>(
      `/api/products/${id}`
    );
    const responseData = response.data?.data || response.data;
    if (!responseData) {
      throw new Error('Invalid response structure from API');
    }
    return responseData;
  },

  /**
   * Lấy chi tiết sản phẩm theo slug
   */
  async getProductBySlug(slug: string): Promise<ProductDetailResponse> {
    const response = await apiClient.get<ApiResponse<ProductDetailResponse>>(
      `/api/products/slug/${slug}`
    );
    const responseData = response.data?.data || response.data;
    if (!responseData) {
      throw new Error('Invalid response structure from API');
    }
    return responseData;
  },

  /**
   * Lấy tất cả danh mục
   */
  async getAllCategories(): Promise<CategoryInfo[]> {
    const response = await apiClient.get<ApiResponse<CategoryInfo[]>>(`/api/categories`);
    const responseData = response.data?.data || response.data;
    if (!responseData) {
      return [];
    }
    return Array.isArray(responseData) ? responseData : [];
  },

  /**
   * Lấy danh mục theo ID
   */
  async getCategoryById(id: number): Promise<CategoryInfo> {
    const response = await apiClient.get<ApiResponse<CategoryInfo>>(`/api/categories/${id}`);
    const responseData = response.data?.data || response.data;
    if (!responseData) {
      throw new Error('Invalid response structure from API');
    }
    return responseData;
  },

  /**
   * Lấy danh mục theo slug
   */
  async getCategoryBySlug(slug: string): Promise<CategoryInfo> {
    const response = await apiClient.get<ApiResponse<CategoryInfo>>(
      `/api/categories/slug/${slug}`
    );
    const responseData = response.data?.data || response.data;
    if (!responseData) {
      throw new Error('Invalid response structure from API');
    }
    return responseData;
  },
};

