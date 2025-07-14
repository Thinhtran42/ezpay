import api from './api';
import { LoginRequest, RegisterRequest, AuthResponse, ChangePasswordRequest } from '../types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', credentials);
    return response.data.data; // Access the data field within the ApiResponse
  },

  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', userData);
    return response.data.data; // Access the data field within the ApiResponse
  },

  changePassword: async (passwordData: ChangePasswordRequest): Promise<void> => {
    await api.put('/auth/change-password', passwordData);
  },

  generateOTP: async (phoneNumber: string): Promise<{ message: string }> => {
    const response = await api.post('/auth/generate-otp', { phoneNumber });
    return response.data; // This returns the ApiResponse object with message
  },

  verifyOTP: async (phoneNumber: string, otp: string): Promise<{ valid: boolean }> => {
    const response = await api.post('/auth/verify-otp', { phoneNumber, otp });
    return response.data.data; // Access the data field within the ApiResponse
  },
}; 