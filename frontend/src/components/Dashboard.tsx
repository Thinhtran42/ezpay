import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router-dom';
import { 
  CreditCardIcon, 
  ClockIcon, 
  CogIcon, 
  ArrowRightOnRectangleIcon,
  ChartBarIcon,
  PlusIcon
} from '@heroicons/react/24/outline';
import NotificationBell from './NotificationBell';

const Dashboard: React.FC = () => {
  const { user, logout, loading } = useAuth();

  const formatCurrency = (amount: number | undefined | null) => {
    const safeAmount = typeof amount === 'number' ? amount : 0;
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(safeAmount);
  };

  const handleLogout = () => {
    logout();
  };

  const isAdmin = user?.role === 'ADMIN';

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <p className="text-gray-500">Không thể tải thông tin người dùng</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">EzPay</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-500">Xin chào, {user?.fullName}</span>
              <NotificationBell />
              <button
                onClick={handleLogout}
                className="flex items-center text-gray-500 hover:text-gray-700"
              >
                <ArrowRightOnRectangleIcon className="h-5 w-5 mr-1" />
                Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Balance Card */}
          <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg shadow-lg p-6 mb-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-blue-100 text-sm font-medium">Số dư hiện tại</p>
                <p className="text-white text-3xl font-bold">{formatCurrency(user?.balance || 0)}</p>
              </div>
              <div className="text-white">
                <CreditCardIcon className="h-12 w-12" />
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <Link
              to="/transfer"
              className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <CreditCardIcon className="h-8 w-8 text-blue-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-900">Chuyển tiền</p>
                  <p className="text-sm text-gray-500">Gửi tiền nhanh chóng</p>
                </div>
              </div>
            </Link>

            <Link
              to="/history"
              className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <ClockIcon className="h-8 w-8 text-green-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-900">Lịch sử</p>
                  <p className="text-sm text-gray-500">Xem giao dịch</p>
                </div>
              </div>
            </Link>

            <Link
              to="/settings"
              className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <CogIcon className="h-8 w-8 text-gray-600" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-900">Cài đặt</p>
                  <p className="text-sm text-gray-500">Quản lý tài khoản</p>
                </div>
              </div>
            </Link>

            {isAdmin && (
              <Link
                to="/admin"
                className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow"
              >
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <ChartBarIcon className="h-8 w-8 text-purple-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-900">Admin</p>
                    <p className="text-sm text-gray-500">Quản trị hệ thống</p>
                  </div>
                </div>
              </Link>
            )}
          </div>

          {/* Admin Quick Actions */}
          {isAdmin && (
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Tính năng Admin</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Link
                  to="/admin/top-up"
                  className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <PlusIcon className="h-6 w-6 text-green-600 mr-3" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">Nạp tiền</p>
                    <p className="text-sm text-gray-500">Nạp tiền cho người dùng</p>
                  </div>
                </Link>
                <Link
                  to="/admin/statistics"
                  className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <ChartBarIcon className="h-6 w-6 text-purple-600 mr-3" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">Thống kê</p>
                    <p className="text-sm text-gray-500">Báo cáo và phân tích</p>
                  </div>
                </Link>
              </div>
            </div>
          )}

          {/* Recent Activity Placeholder */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Hoạt động gần đây</h3>
            <div className="text-center py-8">
              <ClockIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500">Chưa có giao dịch nào</p>
              <Link
                to="/transfer"
                className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                Chuyển tiền ngay
              </Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard; 