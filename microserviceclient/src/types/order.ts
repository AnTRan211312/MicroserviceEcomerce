// Order Types - Matching backend API
export interface OrderItemResponse {
  id: number; // Backend includes id field
  productId: number;
  productName: string;
  productImage?: string; // Backend uses productImage (not optional in backend, but can be null)
  price: number; // BigDecimal from backend
  quantity: number;
}

export interface OrderResponse {
  id: number;
  userId: number;
  orderNumber: string;
  totalAmount: number; // BigDecimal from backend
  status: OrderStatus;
  shippingAddress?: string;
  phone?: string;
  notes?: string;
  createdAt?: string; // ISO string from Instant
  updatedAt?: string; // ISO string from Instant
  items: OrderItemResponse[];
}

export type OrderStatus = 
  | 'PENDING' 
  | 'CONFIRMED' 
  | 'PROCESSING' 
  | 'SHIPPED' 
  | 'DELIVERED' 
  | 'CANCELLED';

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface OrderCreateRequest {
  items: OrderItemRequest[];
  shippingAddress: string;
  phone: string;
  notes?: string;
}

export interface OrderUpdateRequest {
  status: OrderStatus;
  notes?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
