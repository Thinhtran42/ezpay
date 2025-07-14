import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

const EmailVerification: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [isVerifying, setIsVerifying] = useState(true);
  const [verificationStatus, setVerificationStatus] = useState<'success' | 'error' | null>(null);
  const [isResending, setIsResending] = useState(false);
  const [email, setEmail] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    if (token) {
      verifyEmail(token);
    } else {
      setIsVerifying(false);
      setVerificationStatus('error');
    }
  }, [searchParams]);

  const verifyEmail = async (token: string) => {
    try {
      const response = await fetch(`http://localhost:8080/v1/api/auth/verify-email?token=${token}`, {
        method: 'POST',
      });

      const data = await response.json();

      if (response.ok) {
        setVerificationStatus('success');
        toast.success('Email đã được xác nhận thành công!');
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        setVerificationStatus('error');
        toast.error(data.message || 'Xác nhận email thất bại');
      }
    } catch (error) {
      setVerificationStatus('error');
      toast.error('Có lỗi xảy ra khi xác nhận email');
    } finally {
      setIsVerifying(false);
    }
  };

  const resendVerificationEmail = async () => {
    if (!email) {
      toast.error('Vui lòng nhập email');
      return;
    }

    setIsResending(true);
    try {
      const response = await fetch(`http://localhost:8080/v1/api/auth/resend-verification?email=${email}`, {
        method: 'POST',
      });

      const data = await response.json();

      if (response.ok) {
        toast.success('Email xác nhận đã được gửi lại');
      } else {
        toast.error(data.message || 'Gửi lại email thất bại');
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra khi gửi email');
    } finally {
      setIsResending(false);
    }
  };

  if (isVerifying) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-50 flex items-center justify-center px-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-500 mx-auto mb-6"></div>
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Đang xác nhận email...</h2>
          <p className="text-gray-600">Vui lòng đợi trong giây lát</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 text-center">
        {verificationStatus === 'success' ? (
          <>
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">✅ Xác nhận thành công!</h2>
            <p className="text-gray-600 mb-6">
              Email của bạn đã được xác nhận thành công. Tài khoản đã sẵn sàng sử dụng.
            </p>
            <p className="text-sm text-gray-500 mb-6">
              Bạn sẽ được chuyển đến trang đăng nhập sau 3 giây...
            </p>
            <button
              onClick={() => navigate('/login')}
              className="w-full bg-gradient-to-r from-blue-500 to-purple-600 text-white py-3 px-4 rounded-lg font-medium hover:from-blue-600 hover:to-purple-700 transition-all duration-200"
            >
              Đăng nhập ngay
            </button>
          </>
        ) : (
          <>
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">❌ Xác nhận thất bại</h2>
            <p className="text-gray-600 mb-6">
              Link xác nhận không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.
            </p>
            
            <div className="space-y-4">
              <div>
                <input
                  type="email"
                  placeholder="Nhập email để gửi lại"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              
              <button
                onClick={resendVerificationEmail}
                disabled={isResending}
                className="w-full bg-gradient-to-r from-blue-500 to-purple-600 text-white py-3 px-4 rounded-lg font-medium hover:from-blue-600 hover:to-purple-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isResending ? 'Đang gửi...' : 'Gửi lại email xác nhận'}
              </button>
              
              <button
                onClick={() => navigate('/login')}
                className="w-full bg-gray-100 text-gray-700 py-3 px-4 rounded-lg font-medium hover:bg-gray-200 transition-all duration-200"
              >
                Quay lại đăng nhập
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default EmailVerification; 