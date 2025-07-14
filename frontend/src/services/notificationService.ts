import api from './api';
import { Notification } from './websocketService';

export interface ApiResponse<T> {
  code: string;
  message: string;
  data?: T;
}

export const notificationService = {
  // Get all notifications for current user
  getUserNotifications: async (): Promise<Notification[]> => {
    const response = await api.get<ApiResponse<Notification[]>>('/notifications');
    return response.data.data || [];
  },

  // Get unread notifications
  getUnreadNotifications: async (): Promise<Notification[]> => {
    const response = await api.get<ApiResponse<Notification[]>>('/notifications/unread');
    return response.data.data || [];
  },

  // Get recent notifications (last 10)
  getRecentNotifications: async (): Promise<Notification[]> => {
    const response = await api.get<ApiResponse<Notification[]>>('/notifications/recent');
    return response.data.data || [];
  },

  // Get unread count
  getUnreadCount: async (): Promise<number> => {
    const response = await api.get<ApiResponse<number>>('/notifications/unread-count');
    return response.data.data || 0;
  },

  // Mark notification as read
  markAsRead: async (notificationId: number): Promise<void> => {
    await api.put(`/notifications/${notificationId}/read`);
  },

  // Mark all notifications as read
  markAllAsRead: async (): Promise<void> => {
    await api.put('/notifications/read-all');
  },
}; 