package com.thinhtran.EzPay.service;

import com.thinhtran.EzPay.entity.Notification;
import com.thinhtran.EzPay.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    
    /**
     * Create and send notification
     */
    void createAndSendNotification(Long userId, NotificationType type, String title, String message, Long relatedId);
    
    /**
     * Get all notifications for a user
     */
    List<Notification> getUserNotifications(Long userId);
    
    /**
     * Get unread notifications for a user
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * Mark notification as read
     */
    void markAsRead(Long notificationId, Long userId);
    
    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Long userId);
    
    /**
     * Get unread count for a user
     */
    Long getUnreadCount(Long userId);
    
    /**
     * Get recent notifications (last 10)
     */
    List<Notification> getRecentNotifications(Long userId);
} 