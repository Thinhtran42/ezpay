import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/authService';
import { userService } from '../services/userService';
import { 
  ArrowLeftIcon, 
  UserIcon,
  KeyIcon,
  CogIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon
} from '@heroicons/react/24/outline';

interface ProfileFormData {
  fullName: string;
  email: string;
}

interface PasswordFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const Settings: React.FC = () => {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();

  // Profile form state
  const [profileData, setProfileData] = useState<ProfileFormData>({
    fullName: user?.fullName || '',
    email: user?.email || '',
  });
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');

  // Password form state
  const [passwordData, setPasswordData] = useState<PasswordFormData>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');

  // Active tab
  const [activeTab, setActiveTab] = useState<'profile' | 'password'>('profile');

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProfileData({
      ...profileData,
      [e.target.name]: e.target.value,
    });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value,
    });
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileLoading(true);
    setProfileError('');
    setProfileSuccess('');

    try {
      // Update profile via API (assuming we'll implement this)
      const updatedUser = await userService.updateProfile(profileData);
      updateUser(updatedUser);
      setProfileSuccess('Thông tin cá nhân đã được cập nhật thành công!');
    } catch (err: any) {
      setProfileError(err.response?.data?.message || 'Không thể cập nhật thông tin');
    } finally {
      setProfileLoading(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordLoading(true);
    setPasswordError('');
    setPasswordSuccess('');

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setPasswordError('Mật khẩu mới và xác nhận mật khẩu không khớp');
      setPasswordLoading(false);
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setPasswordError('Mật khẩu mới phải có ít nhất 6 ký tự');
      setPasswordLoading(false);
      return;
    }

    try {
      await authService.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      setPasswordSuccess('Mật khẩu đã được thay đổi thành công!');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (err: any) {
      setPasswordError(err.response?.data?.message || 'Không thể thay đổi mật khẩu');
    } finally {
      setPasswordLoading(false);
    }
  };

  const handleBack = () => {
    navigate('/dashboard');
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
            <h1 className="text-2xl font-bold text-gray-900">Cài đặt tài khoản</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Tab Navigation */}
          <div className="bg-white shadow rounded-lg mb-6">
            <div className="border-b border-gray-200">
              <nav className="-mb-px flex">
                <button
                  onClick={() => setActiveTab('profile')}
                  className={`py-4 px-6 text-sm font-medium border-b-2 ${
                    activeTab === 'profile'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <UserIcon className="h-5 w-5 inline mr-2" />
                  Thông tin cá nhân
                </button>
                <button
                  onClick={() => setActiveTab('password')}
                  className={`py-4 px-6 text-sm font-medium border-b-2 ${
                    activeTab === 'password'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <KeyIcon className="h-5 w-5 inline mr-2" />
                  Đổi mật khẩu
                </button>
              </nav>
            </div>

            {/* Profile Tab */}
            {activeTab === 'profile' && (
              <div className="p-6">
                <div className="max-w-md">
                  <h3 className="text-lg font-medium text-gray-900 mb-6 flex items-center">
                    <UserIcon className="h-6 w-6 mr-2" />
                    Thông tin cá nhân
                  </h3>

                  {profileError && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 flex items-center">
                      <ExclamationTriangleIcon className="h-5 w-5 mr-2" />
                      {profileError}
                    </div>
                  )}

                  {profileSuccess && (
                    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4 flex items-center">
                      <CheckCircleIcon className="h-5 w-5 mr-2" />
                      {profileSuccess}
                    </div>
                  )}

                  <form onSubmit={handleProfileSubmit} className="space-y-6">
                    <div>
                      <label htmlFor="userName" className="block text-sm font-medium text-gray-700">
                        Tên đăng nhập
                      </label>
                      <input
                        type="text"
                        name="userName"
                        id="userName"
                        value={user?.userName || ''}
                        disabled
                        className="mt-1 block w-full px-3 py-2 bg-gray-100 border border-gray-300 rounded-md shadow-sm text-gray-500 cursor-not-allowed"
                      />
                      <p className="mt-1 text-sm text-gray-500">Tên đăng nhập không thể thay đổi</p>
                    </div>

                    <div>
                      <label htmlFor="fullName" className="block text-sm font-medium text-gray-700">
                        Họ và tên
                      </label>
                      <input
                        type="text"
                        name="fullName"
                        id="fullName"
                        required
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        value={profileData.fullName}
                        onChange={handleProfileChange}
                      />
                    </div>

                    <div>
                      <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                        Email
                      </label>
                      <input
                        type="email"
                        name="email"
                        id="email"
                        required
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        value={profileData.email}
                        onChange={handleProfileChange}
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Vai trò
                      </label>
                      <input
                        type="text"
                        value={user?.role === 'ADMIN' ? 'Quản trị viên' : 'Người dùng'}
                        disabled
                        className="mt-1 block w-full px-3 py-2 bg-gray-100 border border-gray-300 rounded-md shadow-sm text-gray-500 cursor-not-allowed"
                      />
                    </div>

                    <div className="flex justify-end">
                      <button
                        type="submit"
                        disabled={profileLoading}
                        className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {profileLoading ? 'Đang cập nhật...' : 'Cập nhật thông tin'}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}

            {/* Password Tab */}
            {activeTab === 'password' && (
              <div className="p-6">
                <div className="max-w-md">
                  <h3 className="text-lg font-medium text-gray-900 mb-6 flex items-center">
                    <KeyIcon className="h-6 w-6 mr-2" />
                    Thay đổi mật khẩu
                  </h3>

                  {passwordError && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 flex items-center">
                      <ExclamationTriangleIcon className="h-5 w-5 mr-2" />
                      {passwordError}
                    </div>
                  )}

                  {passwordSuccess && (
                    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4 flex items-center">
                      <CheckCircleIcon className="h-5 w-5 mr-2" />
                      {passwordSuccess}
                    </div>
                  )}

                  <form onSubmit={handlePasswordSubmit} className="space-y-6">
                    <div>
                      <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700">
                        Mật khẩu hiện tại
                      </label>
                      <input
                        type="password"
                        name="currentPassword"
                        id="currentPassword"
                        required
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        value={passwordData.currentPassword}
                        onChange={handlePasswordChange}
                      />
                    </div>

                    <div>
                      <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">
                        Mật khẩu mới
                      </label>
                      <input
                        type="password"
                        name="newPassword"
                        id="newPassword"
                        required
                        minLength={6}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        value={passwordData.newPassword}
                        onChange={handlePasswordChange}
                      />
                      <p className="mt-1 text-sm text-gray-500">Mật khẩu phải có ít nhất 6 ký tự</p>
                    </div>

                    <div>
                      <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                        Xác nhận mật khẩu mới
                      </label>
                      <input
                        type="password"
                        name="confirmPassword"
                        id="confirmPassword"
                        required
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        value={passwordData.confirmPassword}
                        onChange={handlePasswordChange}
                      />
                    </div>

                    <div className="flex justify-end">
                      <button
                        type="submit"
                        disabled={passwordLoading}
                        className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {passwordLoading ? 'Đang thay đổi...' : 'Thay đổi mật khẩu'}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </div>

          {/* Account Info Card */}
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
              <CogIcon className="h-6 w-6 mr-2" />
              Thông tin tài khoản
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <p className="text-sm font-medium text-gray-500">Số dư hiện tại</p>
                <p className="text-2xl font-bold text-blue-600">
                  {new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND',
                  }).format(user?.balance || 0)}
                </p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">Vai trò</p>
                <p className="text-lg font-semibold text-gray-900">
                  {user?.role === 'ADMIN' ? 'Quản trị viên' : 'Người dùng'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Settings; 