// API Configuration
// IMPORTANT: Client must call API through API Gateway
// Gateway automatically adds X-Gateway-Secret header when forwarding to services
// Do NOT call services directly - always use Gateway URL
// 
// In development: Use empty string to leverage Vite proxy (avoids CORS issues)
// In production: Use full Gateway URL
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 
  (import.meta.env.DEV ? '' : 'http://localhost:8080');

// Gateway configuration
// Gateway secret is handled by API Gateway automatically
// Client does not need to send X-Gateway-Secret header
export const GATEWAY_CONFIG = {
  baseUrl: API_BASE_URL,
  // Gateway will automatically add X-Gateway-Secret header
  // Client only needs to ensure it's calling through Gateway
};

export const API_ENDPOINTS = {
  // Auth endpoints
  AUTH: {
    REGISTER: '/api/auth/register',
    LOGIN: '/api/auth/login',
    LOGOUT: '/api/auth/logout',
    ME: '/api/auth/me',
    ME_DETAILS: '/api/auth/me/details',
    REFRESH_TOKEN: '/api/auth/refresh-token',
    SESSIONS: '/api/auth/sessions',
    DELETE_SESSION: (sessionId: string) => `/api/auth/sessions/${sessionId}`,
    FORGOT_PASSWORD: '/api/auth/forgot',
    RESEND_OTP: '/api/auth/resend-otp',
    VERIFY_OTP: '/api/auth/verify-otp',
    RESET_PASSWORD: '/api/auth/reset',
  },
  // User endpoints
  USERS: {
    BASE: '/api/users',
    BY_ID: (id: number) => `/api/users/${id}`,
    UPDATE_PROFILE: '/api/users/me/update-profile',
    UPDATE_PASSWORD: '/api/users/me/update-password',
    UPLOAD_AVATAR: '/api/users/me/upload-avatar',
  },
  // Role endpoints
  ROLES: {
    BASE: '/api/roles',
    BY_ID: (id: number) => `/api/roles/${id}`,
  },
  // Permission endpoints
  PERMISSIONS: {
    BASE: '/api/permissions',
    ALL: '/api/permissions/all',
    BY_ID: (id: number) => `/api/permissions/${id}`,
  },
  // Admin endpoints
  ADMIN: {
    DASHBOARD_STATS: '/api/admin/dashboard/stats',
    MONTHLY_GROWTH: '/api/admin/dashboard/monthly-growth',
    PRODUCTS: {
      BASE: '/api/admin/products',
      BY_ID: (id: number) => `/api/admin/products/${id}`,
      UPLOAD_IMAGE: '/api/admin/products/upload-image',
      UPLOAD_IMAGES: '/api/admin/products/upload-images',
    },
    CATEGORIES: {
      BASE: '/api/admin/categories',
      BY_ID: (id: number) => `/api/admin/categories/${id}`,
    },
  },
  // Order endpoints
  ORDERS: {
    BASE: '/api/orders',
    BY_ID: (id: number) => `/api/orders/${id}`,
    BY_ORDER_NUMBER: (orderNumber: string) => `/api/orders/number/${orderNumber}`,
    MY_ORDERS: '/api/orders/my-orders',
    UPDATE_STATUS: (id: number) => `/api/orders/${id}/status`,
    CANCEL: (id: number) => `/api/orders/${id}/cancel`,
  },
  // Inventory endpoints
  INVENTORY: {
    BASE: '/api/inventory',
    BY_ID: (id: number) => `/api/inventory/${id}`,
    BY_PRODUCT_ID: (productId: number) => `/api/inventory/product/${productId}`,
    ADJUST: (id: number) => `/api/inventory/${id}/adjust`,
    LOW_STOCK: '/api/inventory/low-stock',
    CHECK_AVAILABILITY: (productId: number) => `/api/inventory/product/${productId}/check`,
  },
  // Payment endpoints
  PAYMENTS: {
    CREATE: '/api/payments/create',
    BY_ID: (id: number) => `/api/payments/${id}`,
    BY_ORDER_ID: (orderId: number) => `/api/payments/order/${orderId}`,
    MY_PAYMENTS: '/api/payments/my-payments',
    VNPAY_CALLBACK: '/api/payments/vnpay-callback', // Public endpoint
    ALL: '/api/payments', // Admin endpoint with filters
  },
};

