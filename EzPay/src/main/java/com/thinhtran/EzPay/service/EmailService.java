package com.thinhtran.EzPay.service;

public interface EmailService {
    
    /**
     * Send email verification
     * @param email recipient email
     * @param token verification token
     * @param fullName recipient full name
     */
    void sendEmailVerification(String email, String token, String fullName);
    
    /**
     * Send password reset email
     * @param email recipient email
     * @param token reset token
     * @param fullName recipient full name
     */
    void sendPasswordResetEmail(String email, String token, String fullName);
    
    /**
     * Send welcome email after successful registration
     * @param email recipient email
     * @param fullName recipient full name
     */
    void sendWelcomeEmail(String email, String fullName);
} 