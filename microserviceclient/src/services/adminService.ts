// import { apiClient } from '../lib/axios';
// import { API_ENDPOINTS } from '../config/api';
import type { DashboardResponse } from '../types/dashboard';

export const adminService = {
  /**
   * Lấy thống kê tổng quan dashboard
   * 
   * Khi API sẵn sàng:
   * 1. Uncomment các dòng API call bên dưới
   * 2. Xóa phần mock data và return statement
   * 3. Đảm bảo backend trả về đúng format DashboardResponse
   */
  getDashboardStats: async (): Promise<DashboardResponse> => {
    // TODO: Uncomment khi API sẵn sàng
    // try {
    //   const response = await apiClient.get<{ data: DashboardResponse }>(
    //     API_ENDPOINTS.ADMIN.DASHBOARD_STATS
    //   );
    //   return response.data.data || response.data;
    // } catch (error) {
    //   console.error('Error fetching dashboard stats:', error);
    //   throw error;
    // }
    
    // Mock data - sẽ được thay thế bằng API thực tế
    // Xóa phần này khi API sẵn sàng
    return {
      stats: {
        totalUsers: 25,
        totalProducts: 150,
        pendingOrders: 12,
        monthlyRevenue: 125000000,
        newProductsThisMonth: 8,
        newOrdersThisMonth: 45,
        newCustomersThisMonth: 15,
        conversionRate: 3.2,
        usersChange: -95.7,
        productsChange: 12.5,
        ordersChange: 25.0,
        revenueChange: 18.3,
      },
      monthlyGrowth: [
        { month: "Tháng 1", users: 10, products: 50, orders: 20 },
        { month: "Tháng 2", users: 15, products: 75, orders: 35 },
        { month: "Tháng 3", users: 18, products: 100, orders: 45 },
        { month: "Tháng 4", users: 20, products: 120, orders: 55 },
        { month: "Tháng 5", users: 22, products: 135, orders: 60 },
        { month: "Tháng 6", users: 25, products: 150, orders: 70 },
      ],
    };
  },

  /**
   * Lấy dữ liệu tăng trưởng theo tháng
   * 
   * Khi API sẵn sàng:
   * 1. Uncomment các dòng API call bên dưới
   * 2. Xóa phần mock data và return statement
   * 3. Đảm bảo backend trả về đúng format MonthlyGrowthData[]
   */
  getMonthlyGrowth: async (_months: number = 6): Promise<DashboardResponse['monthlyGrowth']> => {
    // TODO: Uncomment khi API sẵn sàng
    // try {
    //   const response = await apiClient.get<{ data: DashboardResponse['monthlyGrowth'] }>(
    //     `${API_ENDPOINTS.ADMIN.MONTHLY_GROWTH}?months=${months}`
    //   );
    //   return response.data.data || response.data;
    // } catch (error) {
    //   console.error('Error fetching monthly growth:', error);
    //   throw error;
    // }
    
    // Mock data - sẽ được thay thế bằng API thực tế
    // Xóa phần này khi API sẵn sàng
    return [
      { month: "Tháng 1", users: 10, products: 50, orders: 20 },
      { month: "Tháng 2", users: 15, products: 75, orders: 35 },
      { month: "Tháng 3", users: 18, products: 100, orders: 45 },
      { month: "Tháng 4", users: 20, products: 120, orders: 55 },
      { month: "Tháng 5", users: 22, products: 135, orders: 60 },
      { month: "Tháng 6", users: 25, products: 150, orders: 70 },
    ];
  },
};

