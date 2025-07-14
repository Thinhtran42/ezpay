import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface Notification {
  id: number;
  userId: number;
  type: string;
  title: string;
  message: string;
  relatedId?: number;
  isRead: boolean;
  createdAt: string;
}

export type NotificationCallback = (notification: Notification) => void;
export type UnreadCountCallback = (count: number) => void;

class WebSocketService {
  private client: Client | null = null;
  private isConnected = false;
  private userId: string | null = null;
  private notificationCallbacks: NotificationCallback[] = [];
  private unreadCountCallbacks: UnreadCountCallback[] = [];

  connect(userId: string, token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnected && this.userId === userId) {
        resolve();
        return;
      }

      this.disconnect();
      this.userId = userId;

      this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          console.log('WebSocket Debug:', str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.client.onConnect = () => {
        console.log('WebSocket connected');
        this.isConnected = true;
        this.subscribeToNotifications();
        resolve();
      };

      this.client.onStompError = (frame) => {
        console.error('WebSocket error:', frame);
        this.isConnected = false;
        reject(new Error('WebSocket connection failed'));
      };

      this.client.onDisconnect = () => {
        console.log('WebSocket disconnected');
        this.isConnected = false;
      };

      this.client.activate();
    });
  }

  private subscribeToNotifications() {
    if (!this.client || !this.userId) return;

    // Subscribe to notifications
    this.client.subscribe(`/user/${this.userId}/queue/notifications`, (message) => {
      try {
        const notification: Notification = JSON.parse(message.body);
        this.notificationCallbacks.forEach(callback => callback(notification));
      } catch (error) {
        console.error('Error parsing notification:', error);
      }
    });

    // Subscribe to unread count updates
    this.client.subscribe(`/user/${this.userId}/queue/unread-count`, (message) => {
      try {
        const count: number = JSON.parse(message.body);
        this.unreadCountCallbacks.forEach(callback => callback(count));
      } catch (error) {
        console.error('Error parsing unread count:', error);
      }
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.isConnected = false;
    this.userId = null;
    this.notificationCallbacks = [];
    this.unreadCountCallbacks = [];
  }

  onNotification(callback: NotificationCallback) {
    this.notificationCallbacks.push(callback);
    
    // Return unsubscribe function
    return () => {
      const index = this.notificationCallbacks.indexOf(callback);
      if (index > -1) {
        this.notificationCallbacks.splice(index, 1);
      }
    };
  }

  onUnreadCountUpdate(callback: UnreadCountCallback) {
    this.unreadCountCallbacks.push(callback);
    
    // Return unsubscribe function
    return () => {
      const index = this.unreadCountCallbacks.indexOf(callback);
      if (index > -1) {
        this.unreadCountCallbacks.splice(index, 1);
      }
    };
  }

  getConnectionStatus() {
    return this.isConnected;
  }
}

export const websocketService = new WebSocketService(); 