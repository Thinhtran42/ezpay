import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { transactionService } from '../services/transactionService';
import { Transaction } from '../types';
import { 
  ArrowLeftIcon, 
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  CalendarIcon
} from '@heroicons/react/24/outline';

const History: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [filteredTransactions, setFilteredTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<'all' | 'sent' | 'received'>('all');
  const [sortOrder, setSortOrder] = useState<'newest' | 'oldest'>('newest');

  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchTransactions();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [transactions, searchTerm, filterType, sortOrder]);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const data = await transactionService.getHistory();
      setTransactions(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Không thể tải lịch sử giao dịch');
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...transactions];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(transaction => 
        transaction.senderUsername.toLowerCase().includes(searchTerm.toLowerCase()) ||
        transaction.receiverUsername.toLowerCase().includes(searchTerm.toLowerCase()) ||
        transaction.message.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Type filter
    if (filterType === 'sent') {
      filtered = filtered.filter(transaction => transaction.senderUsername === user?.userName);
    } else if (filterType === 'received') {
      filtered = filtered.filter(transaction => transaction.receiverUsername === user?.userName);
    }

    // Sort
    filtered.sort((a, b) => {
      const dateA = new Date(a.createdAt).getTime();
      const dateB = new Date(b.createdAt).getTime();
      return sortOrder === 'newest' ? dateB - dateA : dateA - dateB;
    });

    setFilteredTransactions(filtered);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTransactionIcon = (transaction: Transaction) => {
    const isSent = transaction.senderUsername === user?.userName;
    return isSent ? (
      <ArrowUpIcon className="h-5 w-5 text-red-500" />
    ) : (
      <ArrowDownIcon className="h-5 w-5 text-green-500" />
    );
  };

  const getTransactionText = (transaction: Transaction) => {
    const isSent = transaction.senderUsername === user?.userName;
    return isSent ? 'Gửi đến' : 'Nhận từ';
  };

  const getOtherUser = (transaction: Transaction) => {
    return transaction.senderUsername === user?.userName 
      ? transaction.receiverUsername 
      : transaction.senderUsername;
  };

  const handleBack = () => {
    navigate('/dashboard');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center py-6">
            <button
              onClick={handleBack}
              className="flex items-center text-gray-500 hover:text-gray-700 mr-4"
            >
              <ArrowLeftIcon className="h-5 w-5 mr-1" />
              Quay lại
            </button>
            <h1 className="text-2xl font-bold text-gray-900">Lịch sử giao dịch</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
              {error}
            </div>
          )}

          {/* Search and Filter Bar */}
          <div className="bg-white shadow rounded-lg p-6 mb-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Search */}
              <div className="relative">
                <MagnifyingGlassIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  placeholder="Tìm kiếm giao dịch..."
                  className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>

              {/* Type Filter */}
              <div className="relative">
                <FunnelIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <select
                  className="pl-10 pr-8 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 appearance-none"
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value as 'all' | 'sent' | 'received')}
                >
                  <option value="all">Tất cả giao dịch</option>
                  <option value="sent">Đã gửi</option>
                  <option value="received">Đã nhận</option>
                </select>
              </div>

              {/* Sort */}
              <div className="relative">
                <CalendarIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <select
                  className="pl-10 pr-8 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 appearance-none"
                  value={sortOrder}
                  onChange={(e) => setSortOrder(e.target.value as 'newest' | 'oldest')}
                >
                  <option value="newest">Mới nhất</option>
                  <option value="oldest">Cũ nhất</option>
                </select>
              </div>
            </div>
          </div>

          {/* Transaction List */}
          <div className="bg-white shadow rounded-lg overflow-hidden">
            {filteredTransactions.length === 0 ? (
              <div className="text-center py-12">
                <CalendarIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500 text-lg">
                  {transactions.length === 0 ? 'Chưa có giao dịch nào' : 'Không tìm thấy giao dịch phù hợp'}
                </p>
              </div>
            ) : (
              <div className="divide-y divide-gray-200">
                {filteredTransactions.map((transaction, index) => (
                  <div key={index} className="p-6 hover:bg-gray-50 transition-colors">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="flex-shrink-0">
                          {getTransactionIcon(transaction)}
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {getTransactionText(transaction)} {getOtherUser(transaction)}
                          </p>
                          <p className="text-sm text-gray-500 mt-1">
                            {transaction.message || 'Không có ghi chú'}
                          </p>
                          <p className="text-xs text-gray-400 mt-1">
                            {formatDate(transaction.createdAt)}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className={`text-lg font-semibold ${
                          transaction.senderUsername === user?.userName 
                            ? 'text-red-600' 
                            : 'text-green-600'
                        }`}>
                          {transaction.senderUsername === user?.userName ? '-' : '+'}
                          {formatCurrency(transaction.amount)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Summary */}
          {filteredTransactions.length > 0 && (
            <div className="bg-white shadow rounded-lg p-6 mt-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Tóm tắt</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-center">
                <div>
                  <p className="text-2xl font-bold text-blue-600">{filteredTransactions.length}</p>
                  <p className="text-sm text-gray-500">Tổng giao dịch</p>
                </div>
                <div>
                  <p className="text-2xl font-bold text-red-600">
                    {filteredTransactions.filter(t => t.senderUsername === user?.userName).length}
                  </p>
                  <p className="text-sm text-gray-500">Đã gửi</p>
                </div>
                <div>
                  <p className="text-2xl font-bold text-green-600">
                    {filteredTransactions.filter(t => t.receiverUsername === user?.userName).length}
                  </p>
                  <p className="text-sm text-gray-500">Đã nhận</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default History; 