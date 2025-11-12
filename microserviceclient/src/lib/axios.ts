import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import { API_BASE_URL } from '../config/api';
import { getSessionMeta } from '../utils/sessionHelper';

// Create axios instance
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for cookies (refresh_token)
});

// Request interceptor - Add access token to headers (skip for public endpoints)
apiClient.interceptors.request.use(
  (config) => {
    // Public endpoints that don't require authentication
    const publicEndpoints = [
      '/api/products', // All product endpoints are public
      '/api/categories', // All category endpoints are public
      '/api/auth/login',
      '/api/auth/register',
      '/api/auth/logout', // Logout is public (but needs refresh_token cookie)
      '/api/auth/refresh-token', // Refresh token endpoint (uses httpOnly cookie)
      '/api/auth/forgot',
      '/api/auth/resend-otp',
      '/api/auth/verify-otp',
      '/api/auth/reset',
      // Inventory public endpoints (no @PreAuthorize)
      '/api/inventory/product/', // GET /api/inventory/product/{productId}
      '/api/inventory/check', // GET /api/inventory/product/{productId}/check
      // Payment public endpoints
      '/api/payments/vnpay-callback', // VNPay callback endpoint (public)
    ];
    
    const isPublicEndpoint = publicEndpoints.some((endpoint) => 
      config.url?.startsWith(endpoint)
    );
    
    // Only add token for protected endpoints
    if (!isPublicEndpoint) {
      const token = localStorage.getItem('accessToken');
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log(`🔑 Adding token to request: ${config.method?.toUpperCase()} ${config.url}`);
      } else {
        console.warn(`⚠️ No token found for request: ${config.method?.toUpperCase()} ${config.url}`);
      }
    } else {
      console.log(`🌐 Public endpoint (no token): ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors and token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    // Handle gateway secret errors (should not happen if calling through Gateway)
    if (error.response?.status === 401) {
      const errorMessage = (error.response?.data as any)?.message || '';
      if (errorMessage.includes('gateway secret') || errorMessage.includes('Gateway')) {
        console.error(
          'Gateway Secret Error: Client must call API through API Gateway at',
          API_BASE_URL
        );
        // This should not happen if client is properly configured to use Gateway
        return Promise.reject(
          new Error(
            'Lỗi kết nối: Vui lòng đảm bảo API Gateway đang chạy và cấu hình đúng.'
          )
        );
      }
    }

    // If error is 401 and we haven't retried yet (for token refresh)
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Don't retry refresh-token endpoint itself
      if (originalRequest.url?.includes('/refresh-token')) {
        console.error('❌ Refresh token endpoint returned 401 - invalid refresh token');
        localStorage.removeItem('accessToken');
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      console.log('🔄 401 Unauthorized - Attempting token refresh...');
      
      try {
        // Get session meta from user agent
        const sessionMeta = getSessionMeta();
        
        // Refresh token is httpOnly cookie, so it's automatically sent with withCredentials: true
        // We don't need to manually read or set it
        console.log('🔑 Attempting to refresh token using httpOnly cookie...');
        
        // Call refresh endpoint - cookie is sent automatically
        const response = await apiClient.post(
          '/api/auth/refresh-token',
          sessionMeta,
          {
            // No need to manually set Cookie header - httpOnly cookies are sent automatically
            // withCredentials: true is already set on the axios instance
          }
        );

        // Handle ApiResponse wrapper
        const responseData = (response.data as any)?.data || response.data;
        const accessToken = responseData?.accessToken;
        
        if (accessToken) {
          localStorage.setItem('accessToken', accessToken);
          console.log('✅ Token refreshed successfully');
          
          // Retry original request with new token
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          }
          return apiClient(originalRequest);
        } else {
          console.error('❌ No accessToken in refresh response:', responseData);
          throw new Error('No access token in refresh response');
        }
      } catch (refreshError: any) {
        console.error('❌ Token refresh failed:', refreshError);
        
        // Check if it's a 401 or 400 error (invalid/expired refresh token)
        if (refreshError.response?.status === 401 || refreshError.response?.status === 400) {
          console.warn('⚠️ Refresh token is invalid or expired - redirecting to login');
        }
        
        // Refresh failed, logout user
        localStorage.removeItem('accessToken');
        
        // Only redirect if not already on login page
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

