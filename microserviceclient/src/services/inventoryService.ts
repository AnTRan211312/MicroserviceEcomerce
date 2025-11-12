import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  InventoryResponse,
  InventoryCreateRequest,
  InventoryUpdateRequest,
  InventoryAdjustRequest,
  PageResponse,
} from '../types/inventory';
import type { ApiResponse } from '../types/auth';

export const inventoryService = {
  // Create inventory item
  // Backend wraps InventoryResponse in ApiResponse via ApiResponseAdvice
  createInventory: async (data: InventoryCreateRequest): Promise<InventoryResponse> => {
    const response = await apiClient.post<ApiResponse<InventoryResponse>>(
      API_ENDPOINTS.INVENTORY.BASE,
      data
    );
    return response.data.data;
  },

  // Get inventory by ID
  // Backend wraps InventoryResponse in ApiResponse via ApiResponseAdvice - PUBLIC endpoint
  getInventoryById: async (id: number): Promise<InventoryResponse> => {
    const response = await apiClient.get<ApiResponse<InventoryResponse>>(
      API_ENDPOINTS.INVENTORY.BY_ID(id)
    );
    return response.data.data;
  },

  // Get inventory by product ID
  // Backend wraps InventoryResponse in ApiResponse via ApiResponseAdvice - PUBLIC endpoint
  getInventoryByProductId: async (productId: number): Promise<InventoryResponse> => {
    const response = await apiClient.get<ApiResponse<InventoryResponse>>(
      API_ENDPOINTS.INVENTORY.BY_PRODUCT_ID(productId)
    );
    return response.data.data;
  },

  // Get all inventory items (admin) - with filtering and pagination
  // Backend wraps PageResponseDto<InventoryResponse> in ApiResponse via ApiResponseAdvice
  getAllInventory: async (
    page: number = 0,
    size: number = 20,
    filters?: {
      productId?: number;
      isActive?: boolean;
    }
  ): Promise<PageResponse<InventoryResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (filters?.productId) {
      params.append('productId', filters.productId.toString());
    }
    if (filters?.isActive !== undefined) {
      params.append('isActive', filters.isActive.toString());
    }
    
    const response = await apiClient.get<ApiResponse<PageResponse<InventoryResponse>>>(
      `${API_ENDPOINTS.INVENTORY.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Update inventory
  // Backend wraps InventoryResponse in ApiResponse via ApiResponseAdvice
  updateInventory: async (
    id: number,
    data: InventoryUpdateRequest
  ): Promise<InventoryResponse> => {
    const response = await apiClient.put<ApiResponse<InventoryResponse>>(
      API_ENDPOINTS.INVENTORY.BY_ID(id),
      data
    );
    return response.data.data;
  },

  // Adjust inventory (add or subtract quantity)
  // Backend wraps InventoryResponse in ApiResponse via ApiResponseAdvice
  adjustInventory: async (
    id: number,
    data: InventoryAdjustRequest
  ): Promise<InventoryResponse> => {
    const response = await apiClient.post<ApiResponse<InventoryResponse>>(
      API_ENDPOINTS.INVENTORY.ADJUST(id),
      data
    );
    return response.data.data;
  },

  // Get low stock items
  // Backend wraps List<InventoryResponse> in ApiResponse via ApiResponseAdvice
  getLowStockItems: async (): Promise<InventoryResponse[]> => {
    const response = await apiClient.get<ApiResponse<InventoryResponse[]>>(
      API_ENDPOINTS.INVENTORY.LOW_STOCK
    );
    const responseData = response.data.data || [];
    return Array.isArray(responseData) ? responseData : [];
  },

  // Check availability
  // Backend wraps Boolean in ApiResponse via ApiResponseAdvice - PUBLIC endpoint
  checkAvailability: async (
    productId: number,
    quantity: number
  ): Promise<boolean> => {
    const response = await apiClient.get<ApiResponse<boolean>>(
      `${API_ENDPOINTS.INVENTORY.CHECK_AVAILABILITY(productId)}?quantity=${quantity}`
    );
    return response.data.data ?? false;
  },
};
