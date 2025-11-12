// Dashboard Types
export interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  pendingOrders: number;
  monthlyRevenue: number;
  newProductsThisMonth: number;
  newOrdersThisMonth: number;
  newCustomersThisMonth: number;
  conversionRate: number;
  // Changes compared to last month (percentage)
  usersChange: number;
  productsChange: number;
  ordersChange: number;
  revenueChange: number;
}

export interface MonthlyGrowthData {
  month: string;
  users: number;
  products: number;
  orders: number;
}

export interface DashboardResponse {
  stats: DashboardStats;
  monthlyGrowth: MonthlyGrowthData[];
}

