import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  RoleResponse,
  RoleRequest,
  PageResponse,
} from '../types/role';
import type { ApiResponse } from '../types/auth';

export const roleService = {
  // Create role
  // Backend wraps RoleResponseDto in ApiResponse via ApiResponseAdvice
  createRole: async (data: RoleRequest): Promise<RoleResponse> => {
    const response = await apiClient.post<ApiResponse<RoleResponse>>(
      API_ENDPOINTS.ROLES.BASE,
      data
    );
    return response.data.data;
  },

  // Get all roles (with pagination and filtering)
  // Backend wraps PageResponseDto<RoleResponseDto> in ApiResponse via ApiResponseAdvice
  getAllRoles: async (
    page: number = 0,
    size: number = 5,
    filter?: string
  ): Promise<PageResponse<RoleResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (filter) {
      params.append('filter', filter);
    }
    const response = await apiClient.get<ApiResponse<PageResponse<RoleResponse>>>(
      `${API_ENDPOINTS.ROLES.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Update role
  // Backend wraps RoleResponseDto in ApiResponse via ApiResponseAdvice
  updateRole: async (
    id: number,
    data: RoleRequest
  ): Promise<RoleResponse> => {
    const response = await apiClient.put<ApiResponse<RoleResponse>>(
      API_ENDPOINTS.ROLES.BY_ID(id),
      data
    );
    return response.data.data;
  },

  // Delete role
  // Backend wraps RoleResponseDto in ApiResponse via ApiResponseAdvice
  deleteRole: async (id: number): Promise<RoleResponse> => {
    const response = await apiClient.delete<ApiResponse<RoleResponse>>(
      API_ENDPOINTS.ROLES.BY_ID(id)
    );
    return response.data.data;
  },
};

