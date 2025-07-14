package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.ChangePasswordRequest;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.OTPRequest;
import com.thinhtran.EzPay.dto.request.OTPVerifyRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.dto.response.OTPResponse;
import com.thinhtran.EzPay.dto.response.OTPVerifyResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.exception.AuthenticationException;
import com.thinhtran.EzPay.exception.DuplicateDataException;
import com.thinhtran.EzPay.exception.UserNotFoundException;
import com.thinhtran.EzPay.exception.ValidationException;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.security.JwtTokenProvider;
import com.thinhtran.EzPay.service.AuthService;
import com.thinhtran.EzPay.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;
    private final EmailService emailService;

    // In-memory storage for OTP codes (in production, use Redis or database)
    private final Map<String, String> otpStorage = new HashMap<>();

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new DuplicateDataException("Username", request.getUserName());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateDataException("Email", request.getEmail());
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateDataException("Phone", request.getPhone());
        }

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        
        User user = User.builder()
                .email(request.getEmail())
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.USER)
                .balance(0.0)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusDays(1)) // 24 hours
                .build();

        userRepository.save(user);

        // Send verification email
        emailService.sendEmailVerification(user.getEmail(), verificationToken, user.getFullName());

        // Return auth response with message about email verification
        return new AuthResponse("verification_required");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Check if email is verified (handle null case for existing users)
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthenticationException("Email chưa được xác nhận. Vui lòng kiểm tra email và xác nhận tài khoản.");
        }

        String token = jwtProvider.generateToken(user.getUserName());
        return new AuthResponse(token);
    }

    @Override
    public void changePassword(String userName, ChangePasswordRequest request) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Additional validation: new password should be different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public OTPResponse generateOTP(OTPRequest request) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
        
        // Store OTP in memory (in production, use Redis with expiration)
        otpStorage.put(request.getPhoneNumber(), otp);
        
        // In production, send OTP via SMS service
        return new OTPResponse("OTP đã được gửi đến số điện thoại " + request.getPhoneNumber());
    }

    @Override
    public OTPVerifyResponse verifyOTP(OTPVerifyRequest request) {
        String storedOtp = otpStorage.get(request.getPhoneNumber());
        
        // Check demo OTP first
        if ("123456".equals(request.getOtp())) {
            return new OTPVerifyResponse(true);
        }
        
        if (storedOtp == null) {
            return new OTPVerifyResponse(false);
        }
        
        boolean isValid = storedOtp.equals(request.getOtp());
        
        if (isValid) {
            // Remove OTP after successful verification
            otpStorage.remove(request.getPhoneNumber());
        }
        
        return new OTPVerifyResponse(isValid);
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Token xác nhận không hợp lệ hoặc đã hết hạn"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token xác nhận đã hết hạn");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ValidationException("Email đã được xác nhận");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusDays(1));

        userRepository.save(user);

        // Send verification email
        emailService.sendEmailVerification(user.getEmail(), verificationToken, user.getFullName());
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1)); // 1 hour

        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getFullName());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new ValidationException("Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token đặt lại mật khẩu đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);

        userRepository.save(user);
    }

    @Override
    public void testEmailSending(String email) {
        // Send a test email to verify SendGrid configuration
        emailService.sendEmailVerification(email, "test-token", "Test User");
    }
}
