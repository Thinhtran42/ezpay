package com.thinhtran.EzPay.service.impl;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.thinhtran.EzPay.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from-email:noreply@ezpay.com}")
    private String fromEmail;
    
    @Value("${sendgrid.from-name:EzPay}")
    private String fromName;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void sendEmailVerification(String email, String token, String fullName) {
        String subject = "Xác nhận địa chỉ email - EzPay";
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        
        String htmlContent = buildEmailVerificationHtml(fullName, verificationUrl);
        
        sendEmail(email, subject, htmlContent);
    }
    
    @Override
    public void sendPasswordResetEmail(String email, String token, String fullName) {
        String subject = "Đặt lại mật khẩu - EzPay";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        String htmlContent = buildPasswordResetHtml(fullName, resetUrl);
        
        sendEmail(email, subject, htmlContent);
    }
    
    @Override
    public void sendWelcomeEmail(String email, String fullName) {
        String subject = "Chào mừng đến với EzPay!";
        String htmlContent = buildWelcomeEmailHtml(fullName);
        
        sendEmail(email, subject, htmlContent);
    }
    
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            
            if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
                log.warn("SendGrid API key not configured. Email would be sent to: {}", toEmail);
                log.info("Email Subject: {}", subject);
                log.debug("Email Content: {}", htmlContent);
                return;
            }
            
            log.info("Attempting to send email to: {} with subject: {}", toEmail, subject);
            log.debug("SendGrid API Key configured: {}", sendGridApiKey != null && !sendGridApiKey.isEmpty());
            log.debug("From Email: {}, From Name: {}", fromEmail, fromName);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            log.info("SendGrid API Response - Status Code: {}, Headers: {}", 
                response.getStatusCode(), response.getHeaders());
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to: {} - Status: {}", toEmail, response.getStatusCode());
            } else {
                log.error("Failed to send email to: {} - Status Code: {}, Body: {}, Headers: {}", 
                    toEmail, response.getStatusCode(), response.getBody(), response.getHeaders());
                throw new RuntimeException("Failed to send email. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
            }
            
        } catch (IOException ex) {
            log.error("Error sending email to: {}", toEmail, ex);
        }
    }
    
    private String buildEmailVerificationHtml(String fullName, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác nhận email - EzPay</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">🔒 EzPay</h1>
                    <p style="color: white; margin: 10px 0 0 0; font-size: 16px;">Xác nhận địa chỉ email của bạn</p>
                </div>
                
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p style="font-size: 18px; margin-bottom: 20px;">Xin chào <strong>{{FULL_NAME}}</strong>,</p>
                    
                    <p style="margin-bottom: 20px;">
                        Cảm ơn bạn đã đăng ký tài khoản EzPay! Để hoàn tất quá trình đăng ký, 
                        vui lòng xác nhận địa chỉ email của bạn bằng cách nhấp vào nút bên dưới:
                    </p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{VERIFICATION_URL}}" 
                           style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                  color: white; 
                                  padding: 15px 30px; 
                                  text-decoration: none; 
                                  border-radius: 5px; 
                                  font-weight: bold; 
                                  font-size: 16px;
                                  display: inline-block;">
                            ✅ Xác nhận Email
                        </a>
                    </div>
                    
                    <p style="margin-top: 30px; font-size: 14px; color: #666;">
                        <strong>Lưu ý:</strong> Link xác nhận này sẽ hết hạn sau 24 giờ.
                    </p>
                    
                    <p style="margin-top: 20px; font-size: 14px; color: #666;">
                        Nếu bạn không thể nhấp vào nút, hãy copy và paste link sau vào trình duyệt:<br>
                        <a href="{{VERIFICATION_URL}}" style="color: #667eea; word-break: break-all;">{{VERIFICATION_URL}}</a>
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999; text-align: center;">
                        Email này được gửi từ hệ thống EzPay tự động. Vui lòng không trả lời email này.<br>
                        © 2024 EzPay. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """
            .replace("{{FULL_NAME}}", fullName)
            .replace("{{VERIFICATION_URL}}", verificationUrl);
    }
    
    private String buildPasswordResetHtml(String fullName, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đặt lại mật khẩu - EzPay</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #fc4a1a 0%%, #f7b733 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">🔐 EzPay</h1>
                    <p style="color: white; margin: 10px 0 0 0; font-size: 16px;">Đặt lại mật khẩu</p>
                </div>
                
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p style="font-size: 18px; margin-bottom: 20px;">Xin chào <strong>{{FULL_NAME}}</strong>,</p>
                    
                    <p style="margin-bottom: 20px;">
                        Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản EzPay của bạn. 
                        Nhấp vào nút bên dưới để tạo mật khẩu mới:
                    </p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{RESET_URL}}" 
                           style="background: linear-gradient(135deg, #fc4a1a 0%%, #f7b733 100%%); 
                                  color: white; 
                                  padding: 15px 30px; 
                                  text-decoration: none; 
                                  border-radius: 5px; 
                                  font-weight: bold; 
                                  font-size: 16px;
                                  display: inline-block;">
                            🔑 Đặt lại mật khẩu
                        </a>
                    </div>
                    
                    <p style="margin-top: 30px; font-size: 14px; color: #666;">
                        <strong>Lưu ý quan trọng:</strong><br>
                        • Link này sẽ hết hạn sau 1 giờ<br>
                        • Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này<br>
                        • Để bảo mật tài khoản, chỉ sử dụng link từ email chính thức này
                    </p>
                    
                    <p style="margin-top: 20px; font-size: 14px; color: #666;">
                        Nếu bạn không thể nhấp vào nút, hãy copy và paste link sau vào trình duyệt:<br>
                        <a href="{{RESET_URL}}" style="color: #fc4a1a; word-break: break-all;">{{RESET_URL}}</a>
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999; text-align: center;">
                        Email này được gửi từ hệ thống EzPay tự động. Vui lòng không trả lời email này.<br>
                        © 2024 EzPay. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """
            .replace("{{FULL_NAME}}", fullName)
            .replace("{{RESET_URL}}", resetUrl);
    }
    
    private String buildWelcomeEmailHtml(String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chào mừng đến với EzPay!</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">🎉 EzPay</h1>
                    <p style="color: white; margin: 10px 0 0 0; font-size: 16px;">Chào mừng bạn đến với EzPay!</p>
                </div>
                
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p style="font-size: 18px; margin-bottom: 20px;">Xin chào <strong>{{FULL_NAME}}</strong>,</p>
                    
                    <p style="margin-bottom: 20px;">
                        🎊 Chúc mừng! Tài khoản EzPay của bạn đã được tạo thành công và email đã được xác nhận.
                    </p>
                    
                    <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #4CAF50;">
                        <h3 style="margin-top: 0; color: #4CAF50;">🚀 Bắt đầu sử dụng EzPay ngay!</h3>
                        <ul style="margin: 15px 0; padding-left: 20px;">
                            <li>💰 Chuyển tiền nhanh chóng và an toàn</li>
                            <li>📊 Theo dõi lịch sử giao dịch chi tiết</li>
                            <li>🔒 Bảo mật 2 lớp với OTP</li>
                            <li>⚡ Thông báo realtime</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{LOGIN_URL}}" 
                           style="background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); 
                                  color: white; 
                                  padding: 15px 30px; 
                                  text-decoration: none; 
                                  border-radius: 5px; 
                                  font-weight: bold; 
                                  font-size: 16px;
                                  display: inline-block;">
                            🏠 Đăng nhập ngay
                        </a>
                    </div>
                    
                    <p style="margin-top: 30px; font-size: 14px; color: #666; text-align: center;">
                        <strong>💡 Mẹo:</strong> Để bảo mật tài khoản, hãy thường xuyên thay đổi mật khẩu và không chia sẻ thông tin đăng nhập.
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999; text-align: center;">
                        Cảm ơn bạn đã tin tưởng và sử dụng EzPay!<br>
                        © 2024 EzPay. All rights reserved.
                    </p>
                </div>
            </body>
            </html>
            """
            .replace("{{FULL_NAME}}", fullName)
            .replace("{{LOGIN_URL}}", frontendUrl + "/login");
    }
} 