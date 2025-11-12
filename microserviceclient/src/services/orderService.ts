import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  OrderResponse,
  OrderCreateRequest,
  OrderUpdateRequest,
  PageResponse,
} from '../types/order';
import type { ApiResponse } from '../types/auth';

export const orderService = {
  // Create order
  // Backend wraps OrderResponse in ApiResponse via ApiResponseAdvice
  createOrder: async (data: OrderCreateRequest): Promise<OrderResponse> => {
    const response = await apiClient.post<ApiResponse<OrderResponse>>(
      API_ENDPOINTS.ORDERS.BASE,
      data
    );
    return response.data.data;
  },

  // Get order by ID
  // Backend wraps OrderResponse in ApiResponse via ApiResponseAdvice
  getOrderById: async (id: number): Promise<OrderResponse> => {
    const response = await apiClient.get<ApiResponse<OrderResponse>>(
      API_ENDPOINTS.ORDERS.BY_ID(id)
    );
    return response.data.data;
  },

  // Get order by order number
  // Backend wraps OrderResponse in ApiResponse via ApiResponseAdvice
  getOrderByOrderNumber: async (orderNumber: string): Promise<OrderResponse> => {
    const response = await apiClient.get<ApiResponse<OrderResponse>>(
      API_ENDPOINTS.ORDERS.BY_ORDER_NUMBER(orderNumber)
    );
    return response.data.data;
  },

  // Get my orders (current user)
  // Backend wraps List<OrderResponse> in ApiResponse via ApiResponseAdvice
  getMyOrders: async (): Promise<OrderResponse[]> => {
    const response = await apiClient.get<ApiResponse<OrderResponse[]>>(
      API_ENDPOINTS.ORDERS.MY_ORDERS
    );
    const responseData = response.data.data || [];
    return Array.isArray(responseData) ? responseData : [];
  },

  // Get all orders (admin) - with filtering and pagination
  // Backend wraps PageResponseDto<OrderResponse> in ApiResponse via ApiResponseAdvice
  getAllOrders: async (
    page: number = 0,
    size: number = 20,
    filters?: {
      userId?: number;
      status?: string;
      orderNumber?: string;
    }
  ): Promise<PageResponse<OrderResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (filters?.userId) {
      params.append('userId', filters.userId.toString());
    }
    if (filters?.status) {
      params.append('status', filters.status);
    }
    if (filters?.orderNumber) {
      params.append('orderNumber', filters.orderNumber);
    }
    
    const response = await apiClient.get<ApiResponse<PageResponse<OrderResponse>>>(
      `${API_ENDPOINTS.ORDERS.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Update order status (admin)
  // Backend wraps OrderResponse in ApiResponse via ApiResponseAdvice
  updateOrderStatus: async (
    id: number,
    data: OrderUpdateRequest
  ): Promise<OrderResponse> => {
    const response = await apiClient.put<ApiResponse<OrderResponse>>(
      API_ENDPOINTS.ORDERS.UPDATE_STATUS(id),
      data
    );
    return response.data.data;
  },

  // Cancel order
  cancelOrder: async (id: number): Promise<void> => {
    await apiClient.put(
      API_ENDPOINTS.ORDERS.CANCEL(id),
      {}
    );
  },
};
