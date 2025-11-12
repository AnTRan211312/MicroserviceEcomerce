# Payment Integration Guide

## Tổng quan

Frontend đã được tích hợp với **payment-service** thông qua API Gateway để xử lý thanh toán VNPay.

---

## 📁 Files đã thêm

### 1. **Types** - `src/types/payment.ts`

Định nghĩa các TypeScript types cho payment:

```typescript
import { PaymentStatus, PaymentResponse, PaymentCreateRequest, VnpayPaymentUrlResponse } from './types/payment';
```

**Types bao gồm:**
- `PaymentStatus` - Enum các trạng thái thanh toán
- `PaymentCreateRequest` - Request để tạo thanh toán
- `PaymentResponse` - Response chứa thông tin thanh toán
- `VnpayPaymentUrlResponse` - Response chứa URL thanh toán VNPay

**Helper functions:**
- `getPaymentStatusText(status)` - Lấy text hiển thị cho trạng thái
- `getPaymentStatusColor(status)` - Lấy màu sắc cho trạng thái (dùng cho UI)

---

### 2. **Service** - `src/services/paymentService.ts`

Service để gọi các API payment:

```typescript
import { paymentService } from './services/paymentService';
```

**Methods:**

#### User Endpoints (Cần authentication):

1. **`createPayment(data)`** - Tạo thanh toán và nhận URL VNPay
   ```typescript
   const result = await paymentService.createPayment({
     orderId: 123,
     amount: 1500000,
     orderDescription: 'Thanh toán đơn hàng #ORD-123'
   });
   // Redirect user to VNPay
   window.location.href = result.paymentUrl;
   ```

2. **`getPaymentById(id)`** - Lấy thông tin thanh toán theo ID
   ```typescript
   const payment = await paymentService.getPaymentById(1);
   ```

3. **`getPaymentByOrderId(orderId)`** - Lấy thanh toán theo Order ID
   ```typescript
   const payment = await paymentService.getPaymentByOrderId(123);
   ```

4. **`getMyPayments()`** - Lấy tất cả thanh toán của user
   ```typescript
   const payments = await paymentService.getMyPayments();
   ```

#### Admin Endpoints (Cần quyền admin):

5. **`getAllPayments(params)`** - Lấy tất cả thanh toán với filtering
   ```typescript
   const result = await paymentService.getAllPayments({
     filter: 'status:SUCCESS',
     page: 0,
     size: 20,
     sort: 'createdAt,desc'
   });
   ```

---

### 3. **API Endpoints** - `src/config/api.ts`

Đã thêm payment endpoints:

```typescript
API_ENDPOINTS.PAYMENTS = {
  CREATE: '/api/payments/create',
  BY_ID: (id) => `/api/payments/${id}`,
  BY_ORDER_ID: (orderId) => `/api/payments/order/${orderId}`,
  MY_PAYMENTS: '/api/payments/my-payments',
  VNPAY_CALLBACK: '/api/payments/vnpay-callback',
  ALL: '/api/payments',
}
```

---

### 4. **Axios Configuration** - `src/lib/axios.ts`

Đã thêm VNPay callback vào danh sách public endpoints:

```typescript
const publicEndpoints = [
  // ...
  '/api/payments/vnpay-callback', // VNPay callback (public)
];
```

---

## 🔄 Payment Flow

### 1. User Checkout Flow

```typescript
// Step 1: User clicks "Thanh toán" button
async function handleCheckout(orderId: number, amount: number) {
  try {
    // Step 2: Create payment and get VNPay URL
    const result = await paymentService.createPayment({
      orderId,
      amount,
      orderDescription: `Thanh toán đơn hàng #${orderId}`
    });
    
    // Step 3: Redirect to VNPay
    window.location.href = result.paymentUrl;
    
  } catch (error) {
    console.error('Payment creation failed:', error);
    // Handle error
  }
}
```

### 2. VNPay Callback Flow

Sau khi user thanh toán trên VNPay:

1. VNPay redirect về: `http://localhost:8080/api/payments/vnpay-callback?...`
2. API Gateway forward đến payment-service
3. Payment-service xử lý callback và cập nhật payment status
4. Frontend có thể hiển thị kết quả bằng cách:

```typescript
// Option 1: Get payment by order ID
const payment = await paymentService.getPaymentByOrderId(orderId);
if (payment.status === PaymentStatus.SUCCESS) {
  // Show success message
}

// Option 2: Get payment by ID (từ URL params nếu có)
const paymentId = new URLSearchParams(window.location.search).get('paymentId');
if (paymentId) {
  const payment = await paymentService.getPaymentById(Number(paymentId));
}
```

---

## 💡 Usage Examples

### Example 1: Payment Button Component

```typescript
import { paymentService } from '@/services/paymentService';
import { PaymentCreateRequest } from '@/types/payment';

function PaymentButton({ orderId, amount }: { orderId: number; amount: number }) {
  const [loading, setLoading] = useState(false);
  
  const handlePayment = async () => {
    setLoading(true);
    try {
      const result = await paymentService.createPayment({
        orderId,
        amount,
        orderDescription: `Thanh toán đơn hàng #${orderId}`
      });
      
      // Redirect to VNPay
      window.location.href = result.paymentUrl;
    } catch (error) {
      console.error('Payment failed:', error);
      alert('Không thể tạo thanh toán. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <button onClick={handlePayment} disabled={loading}>
      {loading ? 'Đang xử lý...' : 'Thanh toán VNPay'}
    </button>
  );
}
```

### Example 2: Payment Status Display

```typescript
import { PaymentStatus, getPaymentStatusText, getPaymentStatusColor } from '@/types/payment';

function PaymentStatusBadge({ status }: { status: PaymentStatus }) {
  const text = getPaymentStatusText(status);
  const color = getPaymentStatusColor(status);
  
  return (
    <span className={`badge badge-${color}`}>
      {text}
    </span>
  );
}
```

### Example 3: My Payments Page

```typescript
import { paymentService } from '@/services/paymentService';
import { PaymentResponse } from '@/types/payment';
import { useEffect, useState } from 'react';

function MyPaymentsPage() {
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    loadPayments();
  }, []);
  
  const loadPayments = async () => {
    try {
      const data = await paymentService.getMyPayments();
      setPayments(data);
    } catch (error) {
      console.error('Failed to load payments:', error);
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) return <div>Loading...</div>;
  
  return (
    <div>
      <h1>Lịch sử thanh toán</h1>
      <table>
        <thead>
          <tr>
            <th>Mã thanh toán</th>
            <th>Đơn hàng</th>
            <th>Số tiền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
          </tr>
        </thead>
        <tbody>
          {payments.map(payment => (
            <tr key={payment.id}>
              <td>{payment.vnpayTxnRef}</td>
              <td>#{payment.orderId}</td>
              <td>{payment.amount.toLocaleString('vi-VN')} ₫</td>
              <td>
                <PaymentStatusBadge status={payment.status} />
              </td>
              <td>{new Date(payment.createdAt).toLocaleString('vi-VN')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

### Example 4: Admin - All Payments with Filtering

```typescript
function AdminPaymentsPage() {
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [page, setPage] = useState(0);
  const [filter, setFilter] = useState('');
  
  useEffect(() => {
    loadPayments();
  }, [page, filter]);
  
  const loadPayments = async () => {
    try {
      const result = await paymentService.getAllPayments({
        filter,
        page,
        size: 20,
        sort: 'createdAt,desc'
      });
      setPayments(result.items);
    } catch (error) {
      console.error('Failed to load payments:', error);
    }
  };
  
  return (
    <div>
      <h1>Quản lý thanh toán</h1>
      <input 
        type="text" 
        placeholder="Filter (e.g., status:SUCCESS)" 
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
      />
      {/* Display payments table */}
    </div>
  );
}
```

---

## 🔐 Security Notes

1. **VNPay Callback** là **public endpoint** - không cần JWT token
2. **User endpoints** cần authentication (JWT token tự động thêm bởi axios interceptor)
3. **Admin endpoints** cần quyền `GET /api/payments`
4. Payment chỉ được tạo cho order thuộc về user hiện tại (backend validation)

---

## ⚠️ Important Notes

1. **VNPay Sandbox**: Hiện tại đang dùng môi trường sandbox của VNPay
2. **Return URL**: VNPay sẽ redirect về URL đã cấu hình trong `.env` (backend)
3. **Frontend không handle callback trực tiếp**: VNPay → Backend → Update DB
4. **Frontend chỉ cần**: 
   - Tạo payment → Nhận URL → Redirect
   - Sau khi callback, query payment status để hiển thị kết quả

---

## 📚 Related Documentation

- Backend API: See `payment-service/` documentation
- VNPay Integration: See `payment-service/README.md` (if exists)
- API Gateway: See `GATEWAY_CONFIG.md`

---

## ✅ Checklist

- [x] Types defined (`src/types/payment.ts`)
- [x] Service implemented (`src/services/paymentService.ts`)
- [x] API endpoints configured (`src/config/api.ts`)
- [x] Public endpoint configured (`src/lib/axios.ts`)
- [x] No linter errors
- [ ] UI components (you need to implement based on your design)
- [ ] Payment result page (handle VNPay callback redirect)
- [ ] My Payments page (list user payments)
- [ ] Admin Payments page (optional)

---

## 🚀 Next Steps

1. Implement UI components for payment flow
2. Create payment result page to handle VNPay redirect
3. Add payment status to order detail page
4. Test payment flow in sandbox environment
5. Configure VNPay production credentials when ready

