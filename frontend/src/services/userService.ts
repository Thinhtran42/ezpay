import api from './api';
import { User } from '../types';

export const userService = {
  getProfile: async (): Promise<User> => {
    const response = await api.get('/users/me');
    return response.data.data; // Access the data field within the ApiResponse
  },

  updateProfile: async (userData: Partial<User>): Promise<User> => {
    const response = await api.put('/users/me', userData);
    return response.data.data; // Access the data field within the ApiResponse
  },

  getAllUsers: async (): Promise<User[]> => {
    const response = await api.get('/users');
    return response.data.data; // Access the data field within the ApiResponse
  },

  searchUsers: async (searchTerm: string): Promise<User[]> => {
    const response = await api.get(`/users/search?q=${encodeURIComponent(searchTerm)}`);
    return response.data.data; // Access the data field within the ApiResponse
  },
}; 