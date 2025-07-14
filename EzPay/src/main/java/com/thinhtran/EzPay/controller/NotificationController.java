package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.response.ApiResponse;
import com.thinhtran.EzPay.entity.Notification;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(@AuthenticationPrincipal User user) {
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", notifications));
    }
    
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(@AuthenticationPrincipal User user) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông báo chưa đọc thành công", unreadNotifications));
    }
    
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Notification>>> getRecentNotifications(@AuthenticationPrincipal User user) {
        List<Notification> recentNotifications = notificationService.getRecentNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông báo gần đây thành công", recentNotifications));
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        Long unreadCount = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy số thông báo chưa đọc thành công", unreadCount));
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId, 
                                                       @AuthenticationPrincipal User user) {
        notificationService.markAsRead(notificationId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công"));
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu tất cả đã đọc thành công"));
    }
} 