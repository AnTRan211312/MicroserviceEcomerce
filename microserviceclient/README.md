# Ecommerce Microservice Client

Frontend client cho Ecommerce Microservice system - React + TypeScript + TailwindCSS

## 📁 Cấu trúc dự án

```
src/
├── config/          # Cấu hình API endpoints
│   └── api.ts       # API_BASE_URL và API_ENDPOINTS
├── lib/             # Thư viện và utilities
│   └── axios.ts     # Axios client với interceptors (token refresh, error handling)
├── services/        # API service functions
│   ├── adminService.ts
│   ├── authService.ts
│   ├── inventoryService.ts
│   ├── monitoringService.ts
│   ├── orderService.ts
│   ├── permissionService.ts
│   ├── productService.ts
│   ├── roleService.ts
│   └── userService.ts
├── types/           # TypeScript type definitions
│   ├── auth.ts
│   ├── dashboard.ts
│   ├── inventory.ts
│   ├── monitoring.ts
│   ├── order.ts
│   ├── permission.ts
│   ├── product.ts
│   ├── role.ts
│   └── user.ts
└── utils/           # Utility functions
    ├── formatHelper.ts
    ├── sessionHelper.tsx
    ├── storageHelper.ts
    └── validationHelper.ts
```

## 🚀 Tech Stack

- **React 19** - UI Framework
- **TypeScript** - Type safety
- **TailwindCSS 4** - Styling
- **Axios** - HTTP client
- **Vite** - Build tool

## 📦 Dependencies

### Core
- `react` & `react-dom` - React framework
- `axios` - HTTP client
- `typescript` - TypeScript compiler

### Styling
- `tailwindcss` - Utility-first CSS framework
- `@tailwindcss/vite` - Vite plugin for TailwindCSS

## 🔧 Setup

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

## 📝 Sử dụng

### API Services

Tất cả API services được export từ `src/services/`:

```typescript
import { authService } from './services/authService';
import { productService } from './services/productService';

// Example: Login
const result = await authService.login({ email, password });

// Example: Get products
const products = await productService.getAllProducts();
```

### Types

Tất cả types được định nghĩa trong `src/types/`:

```typescript
import type { UserSessionResponse } from './types/auth';
import type { ProductResponse } from './types/product';
```

### API Configuration

API endpoints được cấu hình trong `src/config/api.ts`:

```typescript
import { API_ENDPOINTS } from './config/api';

// Use endpoints
const url = API_ENDPOINTS.AUTH.LOGIN; // '/api/auth/login'
```

### Axios Client

Axios client đã được cấu hình với:
- Automatic token injection
- Token refresh on 401
- Error handling
- Cookie support (withCredentials)

```typescript
import { apiClient } from './lib/axios';

// Use directly
const response = await apiClient.get('/api/users');
```

## 🌐 API Gateway

Frontend gọi API qua API Gateway tại `http://localhost:8080` (development).

Vite proxy tự động forward requests từ `/api/*` đến API Gateway.

## 📄 Notes

- Tất cả UI components đã được loại bỏ
- Chỉ giữ lại services, types, và cấu hình API
- Có thể tự xây dựng UI components với TailwindCSS
- Axios client đã được cấu hình sẵn với token refresh
