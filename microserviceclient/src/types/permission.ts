// Permission Types - Matching backend API
export interface PermissionResponse {
  id: number;
  name: string;
  apiPath?: string; // Backend returns this field
  method?: string; // Backend returns this field
  module?: string; // Backend returns this field
  createdAt?: string; // Backend returns String, not Instant
  updatedAt?: string; // Backend returns String, not Instant
  // Backend does NOT return description in PermissionResponseDto
}

export interface PermissionRequest {
  name: string;
  description?: string; // May be used in create/update requests
  apiPath?: string;
  method?: string;
  module?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
