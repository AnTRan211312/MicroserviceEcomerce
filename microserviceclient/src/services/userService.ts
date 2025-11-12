import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  DefaultUserResponse,
  UserCreateRequest,
  UserUpdateRequest,
  SelfUserUpdateProfileRequest,
  SelfUpdatePasswordRequest,
  PageResponse,
} from '../types/user';
import type { ApiResponse } from '../types/auth';

export const userService = {
  // Create user
  // Backend wraps DefaultUserResponseDto in ApiResponse via ApiResponseAdvice
  createUser: async (data: UserCreateRequest): Promise<DefaultUserResponse> => {
    const response = await apiClient.post<ApiResponse<DefaultUserResponse>>(
      API_ENDPOINTS.USERS.BASE,
      data
    );
    return response.data.data;
  },

  // Get all users (with pagination and filtering)
  // Backend wraps PageResponseDto<DefaultUserResponseDto> in ApiResponse via ApiResponseAdvice
  getAllUsers: async (
    page: number = 0,
    size: number = 10,
    filter?: string
  ): Promise<PageResponse<DefaultUserResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (filter) {
      params.append('filter', filter);
    }
    const response = await apiClient.get<ApiResponse<PageResponse<DefaultUserResponse>>>(
      `${API_ENDPOINTS.USERS.BASE}?${params.toString()}`
    );
    return response.data.data;
  },

  // Get user by ID
  // Backend wraps DefaultUserResponseDto in ApiResponse via ApiResponseAdvice
  getUserById: async (id: number): Promise<DefaultUserResponse> => {
    const response = await apiClient.get<ApiResponse<DefaultUserResponse>>(
      API_ENDPOINTS.USERS.BY_ID(id)
    );
    return response.data.data;
  },

  // Update user
  // Backend wraps DefaultUserResponseDto in ApiResponse via ApiResponseAdvice
  updateUser: async (data: UserUpdateRequest): Promise<DefaultUserResponse> => {
    const response = await apiClient.put<ApiResponse<DefaultUserResponse>>(
      API_ENDPOINTS.USERS.BASE,
      data
    );
    return response.data.data;
  },

  // Delete user
  // Backend wraps DefaultUserResponseDto in ApiResponse via ApiResponseAdvice
  deleteUser: async (id: number): Promise<DefaultUserResponse> => {
    const response = await apiClient.delete<ApiResponse<DefaultUserResponse>>(
      API_ENDPOINTS.USERS.BY_ID(id)
    );
    return response.data.data;
  },

  // Update self profile
  // Backend wraps DefaultUserResponseDto in ApiResponse via ApiResponseAdvice
  updateSelfProfile: async (
    data: SelfUserUpdateProfileRequest
  ): Promise<DefaultUserResponse> => {
    const response = await apiClient.post<ApiResponse<DefaultUserResponse>>(
      API_ENDPOINTS.USERS.UPDATE_PROFILE,
      data
    );
    return response.data.data;
  },

  // Update self password
  // Backend wraps DefaultUserResponseDto in ApiResponse via ApiResponseAdvice
  updateSelfPassword: async (
    data: SelfUpdatePasswordRequest
  ): Promise<DefaultUserResponse> => {
    const response = await apiClient.post<ApiResponse<DefaultUserResponse>>(
      API_ENDPOINTS.USERS.UPDATE_PASSWORD,
      data
    );
    return response.data.data;
  },

  // Upload avatar
  uploadAvatar: async (file: File): Promise<void> => {
    const formData = new FormData();
    formData.append('avatar', file);
    await apiClient.post(API_ENDPOINTS.USERS.UPLOAD_AVATAR, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

