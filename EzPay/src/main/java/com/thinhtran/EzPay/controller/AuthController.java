package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.request.ChangePasswordRequest;
import com.thinhtran.EzPay.dto.request.ForgotPasswordRequest;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.OTPRequest;
import com.thinhtran.EzPay.dto.request.OTPVerifyRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.request.ResetPasswordRequest;
import com.thinhtran.EzPay.dto.response.ApiResponse;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.dto.response.OTPResponse;
import com.thinhtran.EzPay.dto.response.OTPVerifyResponse;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authResponse));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getUserName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponse<OTPResponse>> generateOTP(@Valid @RequestBody OTPRequest request) {
        OTPResponse otpResponse = authService.generateOTP(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo OTP thành công", otpResponse));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OTPVerifyResponse>> verifyOTP(@Valid @RequestBody OTPVerifyRequest request) {
        OTPVerifyResponse otpVerifyResponse = authService.verifyOTP(request);
        return ResponseEntity.ok(ApiResponse.success("Xác thực OTP thành công", otpVerifyResponse));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận email thành công"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Email xác nhận đã được gửi lại"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Email đặt lại mật khẩu đã được gửi"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.validationError("Mật khẩu xác nhận không khớp"));
        }
        
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công"));
    }

    @PostMapping("/test-email")
    public ResponseEntity<ApiResponse<String>> testEmail(@RequestParam String email) {
        try {
            authService.testEmailSending(email);
            return ResponseEntity.ok(ApiResponse.success("Test email đã được gửi", "Check email và log để xem chi tiết"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("EMAIL_ERROR", "Lỗi gửi email: " + e.getMessage()));
        }
    }
}
