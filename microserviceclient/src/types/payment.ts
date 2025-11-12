// Payment types for frontend
// Corresponds to backend payment-service DTOs

/**
 * Payment Status type
 * Matches backend: com.ecomerce.paymentservice.model.Payment.PaymentStatus
 */
export type PaymentStatus = 
  | 'PENDING'
  | 'PROCESSING'
  | 'SUCCESS'
  | 'FAILED'
  | 'CANCELLED'
  | 'REFUNDED';

export const PaymentStatus = {
  PENDING: 'PENDING' as const,
  PROCESSING: 'PROCESSING' as const,
  SUCCESS: 'SUCCESS' as const,
  FAILED: 'FAILED' as const,
  CANCELLED: 'CANCELLED' as const,
  REFUNDED: 'REFUNDED' as const,
};

/**
 * Request to create a payment
 * Corresponds to: com.ecomerce.paymentservice.dto.request.PaymentCreateRequest
 */
export interface PaymentCreateRequest {
  orderId: number;
  amount: number;
  orderDescription?: string;
}

/**
 * Payment response
 * Corresponds to: com.ecomerce.paymentservice.dto.response.PaymentResponse
 */
export interface PaymentResponse {
  id: number;
  orderId: number;
  userId: number;
  amount: number;
  status: PaymentStatus;
  paymentMethod: string;
  vnpayTxnRef: string;
  vnpayTransactionNo?: string;
  vnpayResponseCode?: string;
  vnpayMessage?: string;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * VNPay Payment URL Response
 * Corresponds to: com.ecomerce.paymentservice.dto.response.VnpayPaymentUrlResponse
 */
export interface VnpayPaymentUrlResponse {
  paymentUrl: string;
  paymentId: number;
  message: string;
}

/**
 * Helper function to get payment status display text
 */
export function getPaymentStatusText(status: PaymentStatus): string {
  const statusMap: Record<PaymentStatus, string> = {
    [PaymentStatus.PENDING]: 'Chờ thanh toán',
    [PaymentStatus.PROCESSING]: 'Đang xử lý',
    [PaymentStatus.SUCCESS]: 'Thành công',
    [PaymentStatus.FAILED]: 'Thất bại',
    [PaymentStatus.CANCELLED]: 'Đã hủy',
    [PaymentStatus.REFUNDED]: 'Đã hoàn tiền',
  };
  return statusMap[status] || status;
}

/**
 * Helper function to get payment status color (for UI)
 */
export function getPaymentStatusColor(status: PaymentStatus): string {
  const colorMap: Record<PaymentStatus, string> = {
    [PaymentStatus.PENDING]: 'yellow',
    [PaymentStatus.PROCESSING]: 'blue',
    [PaymentStatus.SUCCESS]: 'green',
    [PaymentStatus.FAILED]: 'red',
    [PaymentStatus.CANCELLED]: 'gray',
    [PaymentStatus.REFUNDED]: 'purple',
  };
  return colorMap[status] || 'gray';
}

