// Inventory Types - Matching backend API
export interface InventoryResponse {
  id: number;
  productId: number;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  lowStockThreshold: number;
  isActive: boolean;
  isLowStock: boolean;
  createdAt?: string; // ISO string from Instant
  updatedAt?: string; // ISO string from Instant
}

export interface InventoryCreateRequest {
  productId: number;
  quantity: number;
  lowStockThreshold?: number;
}

export interface InventoryUpdateRequest {
  quantity?: number;
  lowStockThreshold?: number;
  isActive?: boolean;
}

export interface InventoryAdjustRequest {
  quantity: number; // Can be positive (add) or negative (subtract)
  reason?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
