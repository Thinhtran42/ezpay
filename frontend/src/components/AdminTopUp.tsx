import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { transactionService } from '../services/transactionService';
import { userService } from '../services/userService';
import { User } from '../types';
import { 
  ArrowLeftIcon, 
  PlusIcon,
  MagnifyingGlassIcon,
  BanknotesIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  UserIcon
} from '@heroicons/react/24/outline';

interface TopUpFormData {
  targetUsername: string;
  amount: string;
}

const AdminTopUp: React.FC = () => {
  const [formData, setFormData] = useState<TopUpFormData>({
    targetUsername: '',
    amount: '',
  });
  const [users, setUsers] = useState<User[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [showUserList, setShowUserList] = useState(false);

  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchUsers();
  }, [user, navigate]);

  useEffect(() => {
    if (searchTerm) {
      const filtered = users.filter(u => 
        u.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.email.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredUsers(filtered);
      setShowUserList(true);
    } else {
      setFilteredUsers([]);
      setShowUserList(false);
    }
  }, [searchTerm, users]);

  const fetchUsers = async () => {
    try {
      const usersData = await userService.getAllUsers();
      // Filter out admin users
      const regularUsers = usersData.filter(u => u.role !== 'ADMIN');
      setUsers(regularUsers);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Không thể tải danh sách người dùng');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'amount') {
      // Only allow numbers and decimal point
      const numericValue = value.replace(/[^\d.]/g, '');
      setFormData({ ...formData, [name]: numericValue });
    } else {
      setFormData({ ...formData, [name]: value });
      if (name === 'targetUsername') {
        setSearchTerm(value);
        setSelectedUser(null);
      }
    }
  };

  const handleUserSelect = (selectedUser: User) => {
    setFormData({ ...formData, targetUsername: selectedUser.userName });
    setSelectedUser(selectedUser);
    setSearchTerm('');
    setShowUserList(false);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    const amount = parseFloat(formData.amount);
    if (isNaN(amount) || amount <= 0) {
      setError('Số tiền không hợp lệ');
      setLoading(false);
      return;
    }

    if (!formData.targetUsername) {
      setError('Vui lòng chọn người dùng');
      setLoading(false);
      return;
    }

    try {
      await transactionService.topUp({
        targetUsername: formData.targetUsername,
        amount: amount,
      });
      
      setSuccess(`Nạp tiền thành công ${formatCurrency(amount)} cho @${formData.targetUsername}`);
      setFormData({
        targetUsername: '',
        amount: '',
      });
      setSelectedUser(null);
      
      // Refresh users list to update balances
      fetchUsers();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Nạp tiền thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    navigate('/admin');
  };

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
            <h1 className="text-2xl font-bold text-gray-900">Nạp tiền cho người dùng</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <main className="max-w-2xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6 flex items-center">
              <ExclamationTriangleIcon className="h-5 w-5 mr-2" />
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6 flex items-center">
              <CheckCircleIcon className="h-5 w-5 mr-2" />
              {success}
            </div>
          )}

          {/* Top-up Form */}
          <div className="bg-white shadow rounded-lg p-6">
            <div className="flex items-center mb-6">
              <PlusIcon className="h-6 w-6 text-green-600 mr-2" />
              <h2 className="text-lg font-medium text-gray-900">Thông tin nạp tiền</h2>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* User Selection */}
              <div className="relative">
                <label htmlFor="targetUsername" className="block text-sm font-medium text-gray-700 mb-2">
                  Người dùng
                </label>
                <div className="relative">
                  <MagnifyingGlassIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    name="targetUsername"
                    id="targetUsername"
                    required
                    className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Tìm kiếm người dùng..."
                    value={formData.targetUsername}
                    onChange={handleInputChange}
                    autoComplete="off"
                  />
                </div>

                {/* User Search Results */}
                {showUserList && filteredUsers.length > 0 && (
                  <div className="absolute z-10 mt-1 w-full bg-white shadow-lg max-h-60 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm">
                    {filteredUsers.slice(0, 10).map((user) => (
                      <div
                        key={user.userName}
                        className="cursor-pointer select-none relative py-2 pl-3 pr-9 hover:bg-blue-50"
                        onClick={() => handleUserSelect(user)}
                      >
                        <div className="flex items-center">
                          <UserIcon className="h-5 w-5 text-gray-400 mr-3" />
                          <div>
                            <div className="font-medium text-gray-900">{user.fullName}</div>
                            <div className="text-sm text-gray-500">@{user.userName} • {user.email}</div>
                            <div className="text-sm text-gray-500">Số dư: {formatCurrency(user.balance)}</div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Selected User Info */}
                {selectedUser && (
                  <div className="mt-3 p-4 bg-blue-50 border border-blue-200 rounded-md">
                    <div className="flex items-center">
                      <UserIcon className="h-6 w-6 text-blue-600 mr-3" />
                      <div>
                        <p className="text-sm font-medium text-blue-900">{selectedUser.fullName}</p>
                        <p className="text-sm text-blue-700">@{selectedUser.userName}</p>
                        <p className="text-sm text-blue-700">Số dư hiện tại: {formatCurrency(selectedUser.balance)}</p>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Amount Input */}
              <div>
                <label htmlFor="amount" className="block text-sm font-medium text-gray-700 mb-2">
                  Số tiền nạp
                </label>
                <div className="relative">
                  <BanknotesIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    name="amount"
                    id="amount"
                    required
                    className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Nhập số tiền (VND)"
                    value={formData.amount}
                    onChange={handleInputChange}
                  />
                </div>
                {formData.amount && !isNaN(parseFloat(formData.amount)) && (
                  <p className="mt-1 text-sm text-gray-500">
                    Số tiền: {formatCurrency(parseFloat(formData.amount))}
                  </p>
                )}
              </div>

              {/* Quick Amount Buttons */}
              <div>
                <p className="text-sm font-medium text-gray-700 mb-3">Số tiền thường dùng:</p>
                <div className="grid grid-cols-3 gap-2">
                  {[50000, 100000, 200000, 500000, 1000000, 2000000].map((amount) => (
                    <button
                      key={amount}
                      type="button"
                      onClick={() => setFormData({ ...formData, amount: amount.toString() })}
                      className="py-2 px-3 text-sm border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      {formatCurrency(amount)}
                    </button>
                  ))}
                </div>
              </div>

              {/* Submit Button */}
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => {
                    setFormData({ targetUsername: '', amount: '' });
                    setSelectedUser(null);
                    setError('');
                    setSuccess('');
                  }}
                  className="py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Đặt lại
                </button>
                <button
                  type="submit"
                  disabled={loading || !formData.targetUsername || !formData.amount}
                  className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Đang nạp tiền...' : 'Xác nhận nạp tiền'}
                </button>
              </div>
            </form>
          </div>

          {/* Recent Users for Quick Selection */}
          <div className="bg-white shadow rounded-lg p-6 mt-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Người dùng gần đây</h3>
            <div className="space-y-2">
              {users.slice(0, 5).map((user) => (
                <div
                  key={user.userName}
                  className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer"
                  onClick={() => handleUserSelect(user)}
                >
                  <div className="flex items-center">
                    <UserIcon className="h-5 w-5 text-gray-400 mr-3" />
                    <div>
                      <p className="text-sm font-medium text-gray-900">{user.fullName}</p>
                      <p className="text-sm text-gray-500">@{user.userName}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">{formatCurrency(user.balance)}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminTopUp; 