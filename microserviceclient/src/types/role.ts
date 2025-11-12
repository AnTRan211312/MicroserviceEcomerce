// Role Types - Matching backend API
import type { PermissionResponse } from './permission';

export interface RoleResponse {
  id: number;
  name: string;
  description?: string;
  active: boolean; // Backend uses boolean, not status enum
  createdAt?: string;
  updatedAt?: string;
  permissions: PermissionResponse[]; // Backend returns List<Permission> (nested class)
}

// Backend uses nested Permission class in RoleResponseDto
export interface RolePermission {
  id: number;
  name: string;
  apiPath?: string;
  method?: string;
  module?: string;
}

export type { PermissionResponse };

export interface RoleRequest {
  name: string;
  description?: string;
  permissionIds?: number[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
