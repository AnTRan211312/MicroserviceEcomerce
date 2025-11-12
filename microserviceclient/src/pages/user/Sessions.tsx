import { useState, useEffect } from 'react';
import { authService } from '../../services/authService';
import type { SessionMetaResponse } from '../../types/auth';

export default function Sessions() {
  const [sessions, setSessions] = useState<SessionMetaResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    try {
      const data = await authService.getAllSessions();
      setSessions(data);
    } catch (error) {
      console.error('Failed to load sessions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (sessionId: string) => {
    if (!confirm('Bạn có chắc chắn muốn xóa phiên đăng nhập này?')) return;

    try {
      await authService.deleteSession(sessionId);
      loadSessions();
    } catch (error) {
      console.error('Failed to delete session:', error);
      alert('Xóa phiên đăng nhập thất bại');
    }
  };

  const getDeviceIcon = (deviceType?: string) => {
    if (!deviceType) return '💻';
    const type = deviceType.toLowerCase();
    if (type === 'mobile' || type === 'smartphone') return '📱';
    if (type === 'tablet') return '📱';
    return '💻';
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'Không xác định';
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-64"></div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
            <div className="h-4 bg-gray-200 rounded mb-4"></div>
            <div className="h-4 bg-gray-200 rounded w-3/4"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Phiên đăng nhập</h1>
        <p className="mt-1 text-sm text-gray-600">Quản lý các phiên đăng nhập của bạn</p>
      </div>

      {/* Sessions List */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {sessions.length === 0 ? (
          <div className="px-6 py-12 text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <p className="text-gray-500 font-medium">Không có phiên đăng nhập nào</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {sessions.map((session) => (
              <div
                key={session.sessionId}
                className="px-6 py-4 hover:bg-gray-50 transition-colors duration-150"
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-start gap-4 flex-1">
                    <div className="flex-shrink-0 w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center text-2xl">
                      {getDeviceIcon(session.deviceType)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="text-sm font-semibold text-gray-900">
                          {session.deviceName || 'Unknown Device'}
                        </h3>
                        {session.current && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            Phiên hiện tại
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-gray-500 mb-1">
                        {session.deviceType || 'Desktop'} • {formatDate(session.loginAt)}
                      </p>
                      {session.userAgent && (
                        <p className="text-xs text-gray-400 font-mono truncate max-w-md">
                          {session.userAgent}
                        </p>
                      )}
                    </div>
                  </div>
                  {!session.current && (
                    <button
                      onClick={() => handleDelete(session.sessionId)}
                      className="ml-4 px-3 py-1.5 text-sm font-medium text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors duration-200"
                    >
                      Xóa
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
