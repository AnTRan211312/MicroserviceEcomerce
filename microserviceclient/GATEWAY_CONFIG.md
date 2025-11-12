# Gateway Configuration

## Tổng quan

Client React này được thiết kế để gọi API thông qua **API Gateway**, không gọi trực tiếp đến các services.

## Cách hoạt động

1. **Client** → Gọi API đến **API Gateway** (http://localhost:8080)
2. **API Gateway** → Tự động thêm header `X-Gateway-Secret` và forward request đến các services
3. **Services** → Validate `X-Gateway-Secret` header từ Gateway

## Cấu hình

### Environment Variables

Tạo file `.env` trong thư mục `microserviceclient`:

```env
# API Gateway URL
VITE_API_BASE_URL=http://localhost:8080
```

### Gateway Secret & JWT Secret

**Lưu ý quan trọng:**
- `GATEWAY_SECRET` và `JWT_SECRET` được cấu hình trong:
  - `api-gateway/src/main/resources/application.properties`
  - `auth-service/src/main/resources/application.properties`
- Client **KHÔNG CẦN** biết hoặc gửi các secret này
- Gateway tự động xử lý `X-Gateway-Secret` header
- Client chỉ cần gửi JWT token (accessToken) trong header `Authorization: Bearer <token>`

## Kiểm tra cấu hình

### 1. Đảm bảo API Gateway đang chạy

```bash
# API Gateway chạy trên port 8080
curl http://localhost:8080/actuator/health
```

### 2. Đảm bảo Auth Service đang chạy và đăng ký với Eureka

### 3. Kiểm tra Gateway Secret trong Gateway và Auth Service

Cả hai service phải có cùng giá trị `GATEWAY_SECRET`:

- `api-gateway/src/main/resources/application.properties`: `gateway.secret=${GATEWAY_SECRET:...}`
- `auth-service/src/main/resources/application.properties`: `gateway.secret=${GATEWAY_SECRET:...}`

## Xử lý lỗi

Nếu gặp lỗi liên quan đến Gateway Secret:
- Đảm bảo API Gateway đang chạy
- Đảm bảo `VITE_API_BASE_URL` trỏ đến Gateway (không phải service trực tiếp)
- Kiểm tra `GATEWAY_SECRET` trong Gateway và Services phải giống nhau

## Flow xác thực

1. User đăng nhập → Client gọi `POST /api/auth/login` qua Gateway
2. Gateway forward request đến Auth Service với header `X-Gateway-Secret`
3. Auth Service validate Gateway Secret và xử lý đăng nhập
4. Auth Service trả về JWT token
5. Client lưu token và gửi trong các request tiếp theo: `Authorization: Bearer <token>`
6. Gateway forward request với cả `X-Gateway-Secret` và `Authorization` header

