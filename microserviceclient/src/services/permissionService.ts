import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  PermissionResponse,
  PermissionRequest,
  PageResponse,
} from '../types/permission';
import type { ApiResponse } from '../types/auth';

export const permissionService = {
  // Create permission
  // Backend wraps PermissionResponseDto in ApiResponse via ApiResponseAdvice
  createPermission: async (
    data: PermissionRequest
  ): Promise<PermissionResponse> => {
    const response = await apiClient.post<ApiResponse<PermissionResponse>>(
      API_ENDPOINTS.PERMISSIONS.BASE,
      data
    );
    return response.data.data;
  },

  // Get all permissions (with pagination and filtering)
  // Backend wraps PageResponseDto<PermissionResponseDto> in ApiResponse via ApiResponseAdvice
  getAllPermissions: async (
    page: number = 0,
    size: number = 5,
    filter?: string
  ): Promise<PageResponse<PermissionResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (filter) {
      params.append('filter', filter);
    }
    const response = await apiClient.get<ApiResponse<PageResponse<PermissionResponse>>>(
      `${API_ENDPOINTS.PERMISSIONS.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Get all permissions (no pagination)
  // Backend wraps List<PermissionResponseDto> in ApiResponse via ApiResponseAdvice
  getAllPermissionsNoPaging: async (
    filter?: string
  ): Promise<PermissionResponse[]> => {
    const params = new URLSearchParams();
    if (filter) {
      params.append('filter', filter);
    }
    const response = await apiClient.get<ApiResponse<PermissionResponse[]>>(
      `${API_ENDPOINTS.PERMISSIONS.ALL}${params.toString() ? `?${params.toString()}` : ''}`
    );
    const responseData = response.data.data || [];
    return Array.isArray(responseData) ? responseData : [];
  },

  // Update permission
  // Backend wraps PermissionResponseDto in ApiResponse via ApiResponseAdvice
  updatePermission: async (
    id: number,
    data: PermissionRequest
  ): Promise<PermissionResponse> => {
    const response = await apiClient.put<ApiResponse<PermissionResponse>>(
      API_ENDPOINTS.PERMISSIONS.BY_ID(id),
      data
    );
    return response.data.data;
  },

  // Delete permission
  // Backend wraps PermissionResponseDto in ApiResponse via ApiResponseAdvice
  deletePermission: async (id: number): Promise<PermissionResponse> => {
    const response = await apiClient.delete<ApiResponse<PermissionResponse>>(
      API_ENDPOINTS.PERMISSIONS.BY_ID(id)
    );
    return response.data.data;
  },
};

