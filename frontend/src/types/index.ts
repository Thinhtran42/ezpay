export interface User {
  id: number;
  userName: string;
  email: string;
  fullName: string;
  phone: string;
  balance: number;
  role?: 'USER' | 'ADMIN';
}

export interface LoginRequest {
  userName: string;
  password: string;
}

export interface RegisterRequest {
  userName: string;
  email: string;
  password: string;
  fullName: string;
  phone: string;
}

export interface TransferRequest {
  receiverUsername: string;
  amount: number;
  message: string;
}

export interface Transaction {
  senderUsername: string;
  receiverUsername: string;
  amount: number;
  message: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface TopUpRequest {
  targetUsername: string;
  amount: number;
}

export interface OTPRequest {
  phoneNumber: string;
  amount: number;
}

export interface OTPVerifyRequest {
  phoneNumber: string;
  otp: string;
}

export interface Statistics {
  totalTransferred: number;
  totalTransactions: number;
  topReceivers: Array<{
    username: string;
    fullName: string;
    totalReceived: number;
    transactionCount: number;
  }>;
} 