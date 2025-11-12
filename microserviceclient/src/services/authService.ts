import { apiClient } from '../lib/axios';
import { API_ENDPOINTS } from '../config/api';
import { getSessionMeta } from '../utils/sessionHelper';
import type {
  UserRegisterRequest,
  UserLoginRequest,
  AuthTokenResponse,
  UserSessionResponse,
  UserProfileResponse,
  SessionMetaResponse,
  ForgotPasswordRequest,
  OtpResponse,
  VerifyOtpRequest,
  VerifyOtpResponse,
  ResetPasswordRequest,
  ResetPasswordResponse,
  ApiResponse,
} from '../types/auth';

export const authService = {
  // Register
  register: async (data: UserRegisterRequest): Promise<UserSessionResponse> => {
    // Backend returns UserSessionResponseDto directly (wrapped by ApiResponseAdvice)
    const response = await apiClient.post<UserSessionResponse | ApiResponse<UserSessionResponse>>(
      API_ENDPOINTS.AUTH.REGISTER,
      data
    );
    // Extract data from ApiResponse wrapper if present
    return (response.data as ApiResponse<UserSessionResponse>).data || (response.data as UserSessionResponse);
  },

  // Login - automatically includes session meta
  login: async (data: UserLoginRequest): Promise<AuthTokenResponse> => {
    // Automatically add session meta from user agent
    const loginData = {
      ...data,
      sessionMetaRequest: getSessionMeta(),
    };
    
    // Backend returns AuthTokenResponseDto directly (not wrapped in ApiResponse)
    // But it's wrapped by ApiResponseAdvice, so we check both
    const response = await apiClient.post<AuthTokenResponse | ApiResponse<AuthTokenResponse>>(
      API_ENDPOINTS.AUTH.LOGIN,
      loginData
    );
    
    // Extract data from ApiResponse wrapper if present
    const responseData = (response.data as ApiResponse<AuthTokenResponse>).data || (response.data as AuthTokenResponse);
    const accessToken = responseData.accessToken;
    
    if (accessToken) {
      localStorage.setItem('accessToken', accessToken);
      console.log('✅ Access token saved to localStorage');
    } else {
      console.error('❌ No accessToken in login response. Full response:', JSON.stringify(response.data, null, 2));
    }
    return responseData;
  },

  // Logout
  logout: async (): Promise<void> => {
    await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
    localStorage.removeItem('accessToken');
  },

  // Get current user session
  getCurrentUser: async (): Promise<UserSessionResponse> => {
    // Backend returns UserSessionResponseDto directly (wrapped by ApiResponseAdvice)
    const response = await apiClient.get<UserSessionResponse | ApiResponse<UserSessionResponse>>(
      API_ENDPOINTS.AUTH.ME
    );
    // Extract data from ApiResponse wrapper if present
    return (response.data as ApiResponse<UserSessionResponse>).data || (response.data as UserSessionResponse);
  },

  // Get current user details
  getCurrentUserDetails: async (): Promise<UserProfileResponse> => {
    // Backend returns UserProfileResponseDto directly (wrapped by ApiResponseAdvice)
    const response = await apiClient.get<UserProfileResponse | ApiResponse<UserProfileResponse>>(
      API_ENDPOINTS.AUTH.ME_DETAILS
    );
    // Extract data from ApiResponse wrapper if present
    return (response.data as ApiResponse<UserProfileResponse>).data || (response.data as UserProfileResponse);
  },

  // Refresh token - automatically includes session meta
  refreshToken: async (): Promise<AuthTokenResponse> => {
    // Automatically get session meta from user agent
    const sessionMeta = getSessionMeta();
    
    // Backend returns AuthTokenResponseDto directly (wrapped by ApiResponseAdvice)
    const response = await apiClient.post<AuthTokenResponse | ApiResponse<AuthTokenResponse>>(
      API_ENDPOINTS.AUTH.REFRESH_TOKEN,
      sessionMeta
    );
    
    // Extract data from ApiResponse wrapper if present
    const responseData = (response.data as ApiResponse<AuthTokenResponse>).data || (response.data as AuthTokenResponse);
    if (responseData.accessToken) {
      localStorage.setItem('accessToken', responseData.accessToken);
      console.log('✅ Access token refreshed and saved');
    } else {
      console.error('❌ No accessToken in refresh response');
    }
    return responseData;
  },

  // Get all sessions
  getAllSessions: async (): Promise<SessionMetaResponse[]> => {
    // Backend returns List<SessionMetaResponseDto> directly (wrapped by ApiResponseAdvice)
    const response = await apiClient.get<SessionMetaResponse[] | ApiResponse<SessionMetaResponse[]>>(
      API_ENDPOINTS.AUTH.SESSIONS
    );
    // Extract data from ApiResponse wrapper if present
    const responseData = (response.data as ApiResponse<SessionMetaResponse[]>).data || (response.data as SessionMetaResponse[]);
    return Array.isArray(responseData) ? responseData : [];
  },

  // Delete session
  deleteSession: async (sessionId: string): Promise<void> => {
    await apiClient.delete(API_ENDPOINTS.AUTH.DELETE_SESSION(sessionId));
  },

  // Forgot password
  forgotPassword: async (data: ForgotPasswordRequest): Promise<OtpResponse> => {
    const response = await apiClient.post<OtpResponse>(
      API_ENDPOINTS.AUTH.FORGOT_PASSWORD,
      data
    );
    return response.data;
  },

  // Resend OTP
  resendOtp: async (data: ForgotPasswordRequest): Promise<OtpResponse> => {
    const response = await apiClient.post<OtpResponse>(
      API_ENDPOINTS.AUTH.RESEND_OTP,
      data
    );
    return response.data;
  },

  // Verify OTP
  verifyOtp: async (data: VerifyOtpRequest): Promise<VerifyOtpResponse> => {
    const response = await apiClient.post<VerifyOtpResponse>(
      API_ENDPOINTS.AUTH.VERIFY_OTP,
      data
    );
    return response.data;
  },

  // Reset password
  resetPassword: async (data: ResetPasswordRequest): Promise<ResetPasswordResponse> => {
    const response = await apiClient.post<ResetPasswordResponse>(
      API_ENDPOINTS.AUTH.RESET_PASSWORD,
      data
    );
    return response.data;
  },
};

