import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  ApiResponse,
  PageResponse,
  ProductAdminResponse,
  ProductListResponse,
  ProductCreateRequest,
  ProductUpdateRequest,
  CategoryAdminResponse,
  CategoryRequest,
} from '../types/product';

export const adminProductService = {
  // Get all products for admin (tối giản - list view)
  async getAllProducts(
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc',
    filters?: Record<string, any>
  ): Promise<PageResponse<ProductListResponse>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort,
    });

    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value.toString());
        }
      });
    }

    const response = await apiClient.get<ApiResponse<PageResponse<ProductListResponse>>>(
      `${API_ENDPOINTS.ADMIN.PRODUCTS.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Get product by ID for admin
  async getProductById(id: number): Promise<ProductAdminResponse> {
    const response = await apiClient.get<ApiResponse<ProductAdminResponse>>(
      API_ENDPOINTS.ADMIN.PRODUCTS.BY_ID(id)
    );
    return response.data.data;
  },

  // Create product (nhận JSON với URLs từ upload riêng)
  async createProduct(data: ProductCreateRequest): Promise<ProductAdminResponse> {
    const response = await apiClient.post<ApiResponse<ProductAdminResponse>>(
      API_ENDPOINTS.ADMIN.PRODUCTS.BASE,
      data
    );
    return response.data.data;
  },

  // Update product (nhận JSON với URLs từ upload riêng)
  async updateProduct(id: number, data: ProductUpdateRequest): Promise<ProductAdminResponse> {
    const response = await apiClient.put<ApiResponse<ProductAdminResponse>>(
      API_ENDPOINTS.ADMIN.PRODUCTS.BY_ID(id),
      data
    );
    return response.data.data;
  },

  // Partial update product (chỉ update fields được gửi lên)
  async patchProduct(id: number, updates: Partial<ProductUpdateRequest>): Promise<ProductAdminResponse> {
    const response = await apiClient.patch<ApiResponse<ProductAdminResponse>>(
      API_ENDPOINTS.ADMIN.PRODUCTS.BY_ID(id),
      updates
    );
    return response.data.data;
  },

  // Delete product
  async deleteProduct(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(API_ENDPOINTS.ADMIN.PRODUCTS.BY_ID(id));
  },

  // Upload single image
  async uploadImage(file: File, productId?: number): Promise<string> {
    const formData = new FormData();
    formData.append('image', file);
    if (productId) {
      formData.append('productId', productId.toString());
    }
    const response = await apiClient.post<ApiResponse<string>>(
      API_ENDPOINTS.ADMIN.PRODUCTS.UPLOAD_IMAGE,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  // Upload multiple images
  async uploadImages(files: File[], productId?: number): Promise<string[]> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('images', file);
    });
    if (productId) {
      formData.append('productId', productId.toString());
    }
    const response = await apiClient.post<ApiResponse<string[]>>(
      API_ENDPOINTS.ADMIN.PRODUCTS.UPLOAD_IMAGES,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },
};

export const adminCategoryService = {
  // Get all categories for admin
  async getAllCategories(
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc',
    filters?: Record<string, any>
  ): Promise<PageResponse<CategoryAdminResponse>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort,
    });

    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value.toString());
        }
      });
    }

    const response = await apiClient.get<ApiResponse<PageResponse<CategoryAdminResponse>>>(
      `${API_ENDPOINTS.ADMIN.CATEGORIES.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Get category by ID for admin
  async getCategoryById(id: number): Promise<CategoryAdminResponse> {
    const response = await apiClient.get<ApiResponse<CategoryAdminResponse>>(
      API_ENDPOINTS.ADMIN.CATEGORIES.BY_ID(id)
    );
    return response.data.data;
  },

  // Create category
  async createCategory(data: CategoryRequest): Promise<CategoryAdminResponse> {
    const response = await apiClient.post<ApiResponse<CategoryAdminResponse>>(
      API_ENDPOINTS.ADMIN.CATEGORIES.BASE,
      data
    );
    return response.data.data;
  },

  // Update category
  async updateCategory(id: number, data: CategoryRequest): Promise<CategoryAdminResponse> {
    const response = await apiClient.put<ApiResponse<CategoryAdminResponse>>(
      API_ENDPOINTS.ADMIN.CATEGORIES.BY_ID(id),
      data
    );
    return response.data.data;
  },

  // Delete category
  async deleteCategory(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(API_ENDPOINTS.ADMIN.CATEGORIES.BY_ID(id));
  },
};

