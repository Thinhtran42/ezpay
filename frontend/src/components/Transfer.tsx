import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { transactionService } from '../services/transactionService';
import { authService } from '../services/authService';
import { userService } from '../services/userService';
import { User } from '../types';
import { ArrowLeftIcon, ShieldCheckIcon } from '@heroicons/react/24/outline';

interface TransferFormData {
  receiverUsername: string;
  amount: string;
  message: string;
}

const Transfer: React.FC = () => {
  const [formData, setFormData] = useState<TransferFormData>({
    receiverUsername: '',
    amount: '',
    message: '',
  });
  const [showOTP, setShowOTP] = useState(false);
  const [otpCode, setOtpCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // User search states
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [showSearchResults, setShowSearchResults] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);

  const { user, updateUser } = useAuth();
  const navigate = useNavigate();

  // Debounced search effect
  useEffect(() => {
    const delayedSearch = setTimeout(async () => {
      if (searchTerm.trim() && searchTerm.length >= 2) {
        setSearchLoading(true);
        try {
          const results = await userService.searchUsers(searchTerm);
          setSearchResults(results);
          setShowSearchResults(true);
        } catch (err) {
          console.error('Search error:', err);
          setSearchResults([]);
        } finally {
          setSearchLoading(false);
        }
      } else {
        setSearchResults([]);
        setShowSearchResults(false);
      }
    }, 300); // 300ms debounce

    return () => clearTimeout(delayedSearch);
  }, [searchTerm]);

  const formatCurrency = (amount: number | undefined | null) => {
    const safeAmount = typeof amount === 'number' ? amount : 0;
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(safeAmount);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    if (name === 'amount') {
      // Only allow numbers and decimal point
      const numericValue = value.replace(/[^\d.]/g, '');
      setFormData({ ...formData, [name]: numericValue });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);
    if (!value.trim()) {
      setSelectedUser(null);
      setFormData({ ...formData, receiverUsername: '' });
      setShowSearchResults(false);
    }
  };

  const handleUserSelect = (selectedUser: User) => {
    setSelectedUser(selectedUser);
    setFormData({ ...formData, receiverUsername: selectedUser.userName });
    setSearchTerm('');
    setShowSearchResults(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const amount = parseFloat(formData.amount);
    if (isNaN(amount) || amount <= 0) {
      setError('Số tiền không hợp lệ');
      setLoading(false);
      return;
    }

    if (amount > (user?.balance || 0)) {
      setError('Số dư không đủ');
      setLoading(false);
      return;
    }

    try {
      // Generate OTP first
      await authService.generateOTP('0123456789'); // Mock phone number
      setShowOTP(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra');
    } finally {
      setLoading(false);
    }
  };

  const handleOTPVerification = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Verify OTP
      const otpResult = await authService.verifyOTP('0123456789', otpCode);
      
      if (otpResult.valid) {
        // Process transfer
        await transactionService.transfer({
          receiverUsername: formData.receiverUsername,
          amount: parseFloat(formData.amount),
          message: formData.message,
        });

        // Update user balance
        const newBalance = (user?.balance || 0) - parseFloat(formData.amount);
        updateUser({ ...user!, balance: newBalance });

        setSuccess('Chuyển tiền thành công!');
        setTimeout(() => {
          navigate('/dashboard');
        }, 2000);
      } else {
        setError('Mã OTP không chính xác');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Chuyển tiền thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    if (showOTP) {
      setShowOTP(false);
      setOtpCode('');
    } else {
      navigate('/dashboard');
    }
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
            <h1 className="text-2xl font-bold text-gray-900">Chuyển tiền</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <main className="max-w-2xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Balance Display */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-blue-900">Số dư hiện tại:</span>
              <span className="text-lg font-bold text-blue-900">
                {formatCurrency(user?.balance || 0)}
              </span>
            </div>
          </div>

          {success && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6">
              {success}
            </div>
          )}

          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
              {error}
            </div>
          )}

          {!showOTP ? (
            /* Transfer Form */
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-6">Thông tin chuyển tiền</h2>
              
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="relative">
                  <label htmlFor="receiverSearch" className="block text-sm font-medium text-gray-700">
                    Người nhận
                  </label>
                  
                  {!selectedUser ? (
                    <div className="relative mt-1">
                      <input
                        type="text"
                        name="receiverSearch"
                        id="receiverSearch"
                        required
                        className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                        placeholder="Tìm kiếm theo tên, email, số điện thoại hoặc username..."
                        value={searchTerm}
                        onChange={handleSearchChange}
                        autoComplete="off"
                      />
                      
                      {searchLoading && (
                        <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                        </div>
                      )}

                      {/* Search Results Dropdown */}
                      {showSearchResults && searchResults.length > 0 && (
                        <div className="absolute z-10 mt-1 w-full bg-white shadow-lg max-h-60 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm">
                          {searchResults.map((user) => (
                            <div
                              key={user.userName}
                              className="cursor-pointer select-none relative py-3 pl-3 pr-9 hover:bg-blue-50"
                              onClick={() => handleUserSelect(user)}
                            >
                              <div className="flex items-center space-x-3">
                                <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                                  <span className="text-sm font-medium text-blue-600">
                                    {user.fullName.charAt(0).toUpperCase()}
                                  </span>
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="font-medium text-gray-900">{user.fullName}</div>
                                  <div className="text-sm text-gray-500">@{user.userName}</div>
                                  <div className="text-sm text-gray-500">{user.email}</div>
                                  {user.phone && (
                                    <div className="text-sm text-gray-500">{user.phone}</div>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}

                      {showSearchResults && searchResults.length === 0 && searchTerm.length >= 2 && !searchLoading && (
                        <div className="absolute z-10 mt-1 w-full bg-white shadow-lg rounded-md py-3 text-center text-sm text-gray-500">
                          Không tìm thấy người dùng nào
                        </div>
                      )}
                    </div>
                  ) : (
                    /* Selected User Display */
                    <div className="mt-1 p-4 bg-blue-50 border border-blue-200 rounded-md">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="flex-shrink-0 w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                            <span className="text-sm font-medium text-blue-600">
                              {selectedUser.fullName.charAt(0).toUpperCase()}
                            </span>
                          </div>
                          <div>
                            <div className="font-medium text-blue-900">{selectedUser.fullName}</div>
                            <div className="text-sm text-blue-700">@{selectedUser.userName}</div>
                            <div className="text-sm text-blue-700">{selectedUser.email}</div>
                            {selectedUser.phone && (
                              <div className="text-sm text-blue-700">{selectedUser.phone}</div>
                            )}
                          </div>
                        </div>
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedUser(null);
                            setFormData({ ...formData, receiverUsername: '' });
                            setSearchTerm('');
                          }}
                          className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                        >
                          Thay đổi
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                <div>
                  <label htmlFor="amount" className="block text-sm font-medium text-gray-700">
                    Số tiền
                  </label>
                  <div className="mt-1 relative">
                    <input
                      type="text"
                      name="amount"
                      id="amount"
                      required
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                      placeholder="0"
                      value={formData.amount}
                      onChange={handleChange}
                    />
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 text-sm">
                      VND
                    </div>
                  </div>
                  {formData.amount && (
                    <p className="mt-1 text-sm text-gray-500">
                      {formatCurrency(parseFloat(formData.amount) || 0)}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="message" className="block text-sm font-medium text-gray-700">
                    Ghi chú
                  </label>
                  <textarea
                    name="message"
                    id="message"
                    rows={3}
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    placeholder="Nhập ghi chú (tùy chọn)"
                    value={formData.message}
                    onChange={handleChange}
                  />
                </div>

                <div className="flex justify-end">
                  <button
                    type="submit"
                    disabled={loading}
                    className="ml-3 inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Đang xử lý...' : 'Tiếp tục'}
                  </button>
                </div>
              </form>
            </div>
          ) : (
            /* OTP Verification */
            <div className="bg-white shadow rounded-lg p-6">
              <div className="text-center mb-6">
                <ShieldCheckIcon className="h-12 w-12 text-blue-600 mx-auto mb-4" />
                <h2 className="text-lg font-medium text-gray-900">Xác thực OTP</h2>
                <p className="text-sm text-gray-500 mt-2">
                  Mã OTP đã được gửi đến số điện thoại của bạn
                </p>
              </div>

              {/* Transfer Summary */}
              <div className="bg-gray-50 rounded-lg p-4 mb-6">
                <h3 className="text-sm font-medium text-gray-900 mb-2">Thông tin chuyển tiền</h3>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Người nhận:</span>
                    <span className="font-medium">{formData.receiverUsername}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Số tiền:</span>
                    <span className="font-medium text-red-600">
                      {formatCurrency(parseFloat(formData.amount))}
                    </span>
                  </div>
                  {formData.message && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Ghi chú:</span>
                      <span className="font-medium">{formData.message}</span>
                    </div>
                  )}
                </div>
              </div>

              <form onSubmit={handleOTPVerification} className="space-y-6">
                <div>
                  <label htmlFor="otpCode" className="block text-sm font-medium text-gray-700">
                    Mã OTP
                  </label>
                  <input
                    type="text"
                    name="otpCode"
                    id="otpCode"
                    required
                    maxLength={6}
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm text-center text-lg tracking-widest"
                    placeholder="000000"
                    value={otpCode}
                    onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ''))}
                  />
                  <p className="mt-2 text-sm text-gray-500">
                    Nhập mã OTP 6 chữ số (demo: 123456)
                  </p>
                </div>

                <div className="flex justify-end space-x-3">
                  <button
                    type="button"
                    onClick={handleBack}
                    className="py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    Quay lại
                  </button>
                  <button
                    type="submit"
                    disabled={loading || otpCode.length !== 6}
                    className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Đang xử lý...' : 'Xác nhận chuyển tiền'}
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default Transfer; 