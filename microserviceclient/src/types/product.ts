// Product Types - Matching backend API
export interface ProductSummaryResponse {
  id: number;
  name: string;
  thumbnail: string;
  price: number; // BigDecimal from backend
  discountPrice: number; // BigDecimal from backend (not optional in backend)
}

export interface ProductDetailResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
  price: number; // BigDecimal from backend
  discountPrice?: number; // BigDecimal from backend
  discountStartDate?: string; // LocalDateTime from backend
  discountEndDate?: string; // LocalDateTime from backend
  thumbnail: string;
  images: string[];
  category: CategoryInfo;
  active: boolean; // boolean (not optional)
  featured?: boolean; // Boolean (nullable in backend)
}

// Product List Response - tối giản cho list view
export interface ProductListResponse {
  id: number;
  name: string;
  price: number;
  discountPrice?: number;
  thumbnail: string;
  categoryName: string; // Chỉ tên category, không phải object
  active: boolean;
  featured?: boolean;
}

// Admin Product Response - có thêm createdAt, updatedAt (cho detail view)
export interface ProductAdminResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
  price: number;
  discountPrice?: number;
  discountStartDate?: string;
  discountEndDate?: string;
  thumbnail: string;
  images: string[];
  category: CategoryInfo;
  active: boolean;
  featured?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductCreateRequest {
  name: string;
  description?: string;
  price: number;
  discountPrice?: number;
  discountStartDate?: string;
  discountEndDate?: string;
  categoryId: number;
  active: boolean;
  featured?: boolean;
  thumbnailUrl?: string; // URL từ upload riêng
  imageUrls?: string[]; // URLs từ upload riêng
}

export interface ProductUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  discountPrice?: number;
  discountStartDate?: string;
  discountEndDate?: string;
  categoryId?: number;
  active?: boolean;
  featured?: boolean;
  thumbnailUrl?: string; // URL từ upload riêng
  imageUrls?: string[]; // URLs từ upload riêng
}

export interface CategoryInfo {
  id: number;
  name: string;
  slug: string;
  // Backend does NOT return description in CategoryInfo
}

// Admin Category Response - có thêm createdAt, updatedAt
export interface CategoryAdminResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiResponse<T> {
  message: string;
  errorCode: string | null; // Backend uses errorCode, not error
  data: T;
}
