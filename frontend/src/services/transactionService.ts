import api from './api';
import { TransferRequest, Transaction, Statistics, TopUpRequest } from '../types';

export const transactionService = {
  transfer: async (transferData: TransferRequest): Promise<{ message: string }> => {
    const response = await api.post('/transactions', transferData);
    return response.data; // This returns the ApiResponse object with message
  },

  getHistory: async (): Promise<Transaction[]> => {
    const response = await api.get('/transactions');
    return response.data.data; // Access the data field within the ApiResponse
  },

  topUp: async (topUpData: TopUpRequest): Promise<{ message: string }> => {
    const response = await api.post('/transactions/top-up', topUpData);
    return response.data; // This returns the ApiResponse object with message
  },

  getStatistics: async (): Promise<Statistics> => {
    const response = await api.get('/transactions/statistics');
    return response.data.data; // Access the data field within the ApiResponse
  },
}; 