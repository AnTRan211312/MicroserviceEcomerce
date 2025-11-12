import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import type {
  PaymentCreateRequest,
  PaymentResponse,
  VnpayPaymentUrlResponse,
} from '../types/payment';
import type { ApiResponse } from '../types/auth';
import type { PageResponse } from '../types/product';

/**
 * Payment Service
 * Handles all payment-related API calls to payment-service via API Gateway
 */
export const paymentService = {
  // ==================== USER ENDPOINTS ====================
  
  /**
   * Create a payment and get VNPay payment URL
   * POST /api/payments/create
   * 
   * @param data - Payment creation data (orderId, amount, orderDescription)
   * @returns VNPay payment URL to redirect user to
   * 
   * @example
   * const result = await paymentService.createPayment({
   *   orderId: 123,
   *   amount: 1500000,
   *   orderDescription: 'Thanh toán đơn hàng #ORD-123'
   * });
   * // Redirect user to result.paymentUrl
   * window.location.href = result.paymentUrl;
   */
  createPayment: async (data: PaymentCreateRequest): Promise<VnpayPaymentUrlResponse> => {
    const response = await apiClient.post<ApiResponse<VnpayPaymentUrlResponse>>(
      API_ENDPOINTS.PAYMENTS.CREATE,
      data
    );
    // Extract data from ApiResponse wrapper
    return response.data.data || (response.data as unknown as VnpayPaymentUrlResponse);
  },
  
  /**
   * Get payment by ID (user can only get their own payments)
   * GET /api/payments/{id}
   * 
   * @param id - Payment ID
   * @returns Payment details
   */
  getPaymentById: async (id: number): Promise<PaymentResponse> => {
    const response = await apiClient.get<ApiResponse<PaymentResponse>>(
      API_ENDPOINTS.PAYMENTS.BY_ID(id)
    );
    return response.data.data || (response.data as unknown as PaymentResponse);
  },
  
  /**
   * Get payment by order ID (user can only get payment for their own order)
   * GET /api/payments/order/{orderId}
   * 
   * @param orderId - Order ID
   * @returns Payment details for the order
   */
  getPaymentByOrderId: async (orderId: number): Promise<PaymentResponse> => {
    const response = await apiClient.get<ApiResponse<PaymentResponse>>(
      API_ENDPOINTS.PAYMENTS.BY_ORDER_ID(orderId)
    );
    return response.data.data || (response.data as unknown as PaymentResponse);
  },
  
  /**
   * Get all payments of current user
   * GET /api/payments/my-payments
   * 
   * @returns List of user's payments
   */
  getMyPayments: async (): Promise<PaymentResponse[]> => {
    const response = await apiClient.get<ApiResponse<PaymentResponse[]>>(
      API_ENDPOINTS.PAYMENTS.MY_PAYMENTS
    );
    const data = response.data.data || (response.data as unknown as PaymentResponse[]);
    return Array.isArray(data) ? data : [];
  },
  
  // ==================== ADMIN ENDPOINTS ====================
  
  /**
   * Get all payments with filtering (Admin only)
   * GET /api/payments
   * Requires permission: GET /api/payments
   * 
   * @param params - Query parameters for filtering and pagination
   * @param params.filter - Filter string (e.g., "status:SUCCESS", "userId:123")
   * @param params.page - Page number (0-based)
   * @param params.size - Page size
   * @param params.sort - Sort field and direction (e.g., "createdAt,desc")
   * @returns Paginated list of payments
   * 
   * @example
   * // Get successful payments, page 0, 20 items, sorted by creation date
   * const payments = await paymentService.getAllPayments({
   *   filter: 'status:SUCCESS',
   *   page: 0,
   *   size: 20,
   *   sort: 'createdAt,desc'
   * });
   */
  getAllPayments: async (params?: {
    filter?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PageResponse<PaymentResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<PaymentResponse>>>(
      API_ENDPOINTS.PAYMENTS.ALL,
      { params }
    );
    return response.data.data || (response.data as unknown as PageResponse<PaymentResponse>);
  },
};

