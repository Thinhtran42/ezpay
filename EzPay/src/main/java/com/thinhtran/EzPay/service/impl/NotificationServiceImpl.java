package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.entity.Notification;
import com.thinhtran.EzPay.entity.NotificationType;
import com.thinhtran.EzPay.exception.UserNotFoundException;
import com.thinhtran.EzPay.repository.NotificationRepository;
import com.thinhtran.EzPay.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void createAndSendNotification(Long userId, NotificationType type, String title, String message, Long relatedId) {
        // Create notification in database
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send realtime notification via WebSocket
        sendRealtimeNotification(userId, savedNotification);
        
        log.info("Notification created and sent to user {}: {}", userId, title);
    }
    
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }
    
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
        
        // Send updated unread count via WebSocket
        sendUnreadCountUpdate(userId);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
        
        // Send updated unread count via WebSocket
        sendUnreadCountUpdate(userId);
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    @Override
    public List<Notification> getRecentNotifications(Long userId) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }
    
    private void sendRealtimeNotification(Long userId, Notification notification) {
        try {
            // Send to specific user's private channel
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Also send unread count update
            sendUnreadCountUpdate(userId);
            
        } catch (Exception e) {
            log.error("Error sending realtime notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    private void sendUnreadCountUpdate(Long userId) {
        try {
            Long unreadCount = getUnreadCount(userId);
            
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    unreadCount
            );
            
        } catch (Exception e) {
            log.error("Error sending unread count update to user {}: {}", userId, e.getMessage());
        }
    }
} 