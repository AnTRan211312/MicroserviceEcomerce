import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { authService } from '../../services/authService';
import { userService } from '../../services/userService';
import type { UserProfileResponse } from '../../types/auth';

export default function Profile() {
  const { user, refreshUser } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    dateBirth: '',
    address: '',
    gender: '' as 'MALE' | 'FEMALE' | 'OTHER' | '',
  });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const data = await authService.getCurrentUserDetails();
      setProfile(data);
      setFormData({
        name: data.name,
        dateBirth: data.dateBirth || '',
        address: data.address || '',
        gender: data.gender || '',
      });
    } catch (error) {
      console.error('Failed to load profile:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      const updateData = {
        ...formData,
        gender: formData.gender || undefined, // Convert empty string to undefined
      };
      await userService.updateSelfProfile(updateData);
      await loadProfile();
      await refreshUser();
      setIsEditing(false);
      alert('Cập nhật thông tin thành công');
    } catch (error) {
      console.error('Failed to update profile:', error);
      alert('Cập nhật thông tin thất bại');
    }
  };

  const handleUploadAvatar = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      await userService.uploadAvatar(file);
      await loadProfile();
      await refreshUser();
      alert('Cập nhật avatar thành công');
    } catch (error) {
      console.error('Failed to upload avatar:', error);
      alert('Cập nhật avatar thất bại');
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-64"></div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
            <div className="h-24 bg-gray-200 rounded-full w-24 mb-6"></div>
            <div className="space-y-4">
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-4 bg-gray-200 rounded w-1/2"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Thông tin cá nhân</h1>
        <p className="mt-1 text-sm text-gray-600">Quản lý thông tin tài khoản của bạn</p>
      </div>

      {/* Profile Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sm:p-8">
        {/* Avatar Section */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6 mb-8 pb-8 border-b border-gray-200">
          <div className="relative">
            {profile?.logoUrl ? (
              <img
                src={profile.logoUrl}
                alt={user?.name}
                className="w-24 h-24 rounded-full object-cover border-4 border-gray-100"
              />
            ) : (
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-blue-400 to-indigo-600 flex items-center justify-center text-3xl font-bold text-white border-4 border-gray-100">
                {user?.name?.[0]?.toUpperCase() || 'U'}
              </div>
            )}
            <label className="absolute bottom-0 right-0 bg-blue-600 text-white rounded-full p-2.5 cursor-pointer hover:bg-blue-700 transition-colors duration-200 shadow-lg">
              <input
                type="file"
                accept="image/*"
                onChange={handleUploadAvatar}
                className="hidden"
              />
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </label>
          </div>
          <div className="flex-1">
            <h2 className="text-2xl font-bold text-gray-900 mb-1">{user?.name}</h2>
            <p className="text-gray-600">{user?.email}</p>
          </div>
        </div>

        {/* Form */}
        {isEditing ? (
          <div className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Họ và tên</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="block w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Ngày sinh</label>
              <input
                type="date"
                value={formData.dateBirth}
                onChange={(e) => setFormData({ ...formData, dateBirth: e.target.value })}
                className="block w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Địa chỉ</label>
              <input
                type="text"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                className="block w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Giới tính</label>
              <select
                value={formData.gender}
                onChange={(e) => setFormData({ ...formData, gender: e.target.value as any })}
                className="block w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
              >
                <option value="">Chọn giới tính</option>
                <option value="MALE">Nam</option>
                <option value="FEMALE">Nữ</option>
                <option value="OTHER">Khác</option>
              </select>
            </div>
            <div className="flex gap-3 pt-4">
              <button
                onClick={handleSave}
                className="px-6 py-2.5 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors duration-200"
              >
                Lưu thay đổi
              </button>
              <button
                onClick={() => {
                  setIsEditing(false);
                  loadProfile();
                }}
                className="px-6 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              >
                Hủy
              </button>
            </div>
          </div>
        ) : (
          <div className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Email</label>
              <p className="text-base text-gray-900">{profile?.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Họ và tên</label>
              <p className="text-base text-gray-900">{profile?.name}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Ngày sinh</label>
              <p className="text-base text-gray-900">
                {profile?.dateBirth
                  ? new Date(profile.dateBirth).toLocaleDateString('vi-VN')
                  : 'Chưa cập nhật'}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Địa chỉ</label>
              <p className="text-base text-gray-900">{profile?.address || 'Chưa cập nhật'}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Giới tính</label>
              <p className="text-base text-gray-900">
                {profile?.gender === 'MALE'
                  ? 'Nam'
                  : profile?.gender === 'FEMALE'
                  ? 'Nữ'
                  : profile?.gender === 'OTHER'
                  ? 'Khác'
                  : 'Chưa cập nhật'}
              </p>
            </div>
            <div className="pt-4">
              <button
                onClick={() => setIsEditing(true)}
                className="px-6 py-2.5 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors duration-200"
              >
                Chỉnh sửa
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
