// Auth Types - Matching backend API
export interface UserRegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface UserLoginRequest {
  email: string;
  password: string;
  sessionMetaRequest?: SessionMetaRequest; // Automatically added by authService.login()
}

// ApiResponse wrapper from backend
export interface ApiResponse<T> {
  message: string;
  errorCode?: string | null;
  data: T;
}

export interface AuthTokenResponse {
  user: UserSessionResponse; // Backend uses "user" field
  accessToken: string;
  // refreshToken is in HttpOnly cookie, not in response body
  // tokenType is not in backend response
}

export interface UserSessionResponse {
  id: number;
  email: string;
  name: string;
  logoUrl?: string;
  role: string; // Backend returns single role as String, not List<RoleResponse>
  permissions: string[]; // Backend returns List<String>, not List<PermissionResponse>
  updatedAt?: string;
}

export interface UserProfileResponse {
  id: number;
  email: string;
  name: string;
  dateBirth?: string; // LocalDate from backend (YYYY-MM-DD)
  address?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  logoUrl?: string;
  createdAt?: string; // Instant from backend
  updatedAt?: string; // Instant from backend
  // Backend does NOT return roles and permissions in UserProfileResponseDto
}

export interface SessionMetaRequest {
  userAgent?: string;
  ipAddress?: string;
  deviceName?: string;
  deviceType?: string;
}

export interface SessionMetaResponse {
  sessionId: string; // Backend uses sessionId, not id
  userAgent?: string;
  deviceName?: string;
  deviceType?: string;
  loginAt?: string; // Instant from backend
  current: boolean; // Indicates if this is the current session
  // Backend does NOT return id, ipAddress, lastAccessedAt
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface OtpResponse {
  message: string;
  otpId?: string;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export interface VerifyOtpResponse {
  message: string;
  verified: boolean;
}

export interface ResetPasswordRequest {
  email: string;
  otp: string;
  newPassword: string;
}

export interface ResetPasswordResponse {
  message: string;
}
