// User Types
import type { RoleResponse, PermissionResponse } from './role';

export interface DefaultUserResponse {
  id: number;
  email: string;
  name: string;
  phone?: string;
  dob?: string;
  address?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  logoUrl?: string;
  roles: RoleResponse[];
  permissions: PermissionResponse[];
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreateRequest {
  email: string;
  password: string;
  name: string;
  phone?: string;
  dob?: string;
  address?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  roleIds?: number[];
}

export interface UserUpdateRequest {
  id: number;
  email?: string;
  name?: string;
  phone?: string;
  dob?: string;
  address?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  roleIds?: number[];
}

export interface SelfUserUpdateProfileRequest {
  username?: string; // Backend uses username, not name
  birthDate?: string; // Backend uses birthDate (LocalDate), not dob
  address?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  // Backend does NOT have phone field
}

export interface SelfUpdatePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

