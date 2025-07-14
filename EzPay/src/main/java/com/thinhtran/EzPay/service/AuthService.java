package com.thinhtran.EzPay.service;

import com.thinhtran.EzPay.dto.request.ChangePasswordRequest;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.OTPRequest;
import com.thinhtran.EzPay.dto.request.OTPVerifyRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.dto.response.OTPResponse;
import com.thinhtran.EzPay.dto.response.OTPVerifyResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(String userName, ChangePasswordRequest request);

    OTPResponse generateOTP(OTPRequest request);

    OTPVerifyResponse verifyOTP(OTPVerifyRequest request);

    // Email verification methods
    void verifyEmail(String token);
    
    void resendVerificationEmail(String email);
    
    // Forgot password methods
    void forgotPassword(String email);
    
    void resetPassword(String token, String newPassword);
    
    // Test email sending
    void testEmailSending(String email);
}
