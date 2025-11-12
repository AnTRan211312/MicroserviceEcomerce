# 🚀 Hướng dẫn khởi động các services

## ⚠️ Vấn đề hiện tại
Frontend đang cố kết nối đến API Gateway tại `http://localhost:8080` nhưng Gateway chưa chạy.

## 📋 Các bước khởi động

### 1. Khởi động Eureka Server (Port 8761)
```bash
# Chạy trong IntelliJ IDEA hoặc:
cd eureka-server
mvn spring-boot:run
```

### 2. Khởi động API Gateway (Port 8080) - **QUAN TRỌNG**
```bash
# Chạy trong IntelliJ IDEA hoặc:
cd api-gateway
mvn spring-boot:run
```

**Lưu ý:** API Gateway phải chạy TRƯỚC các microservices khác vì nó cần kết nối với Eureka.

### 3. Khởi động các Microservices (theo thứ tự)

#### Auth Service (Port 8081)
```bash
cd auth-service
mvn spring-boot:run
```

#### Product Service (Port 8082)
```bash
cd product-service
mvn spring-boot:run
```

#### Order Service (Port 8083)
```bash
cd order-service
mvn spring-boot:run
```

#### Inventory Service (Port 8084)
```bash
cd inventory-service
mvn spring-boot:run
```

### 4. Kiểm tra services đã chạy
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080/actuator/health
- Auth Service: http://localhost:8081/actuator/health
- Product Service: http://localhost:8082/actuator/health

## 🔧 Giải pháp tạm thời (nếu không có API Gateway)

Nếu bạn muốn test frontend mà không cần API Gateway, có thể cấu hình Vite proxy trực tiếp đến các services:

Xem file `vite.config.ts` để cấu hình proxy trực tiếp.

## 📝 Thứ tự khởi động khuyến nghị

1. **Eureka Server** (8761)
2. **API Gateway** (8080) - Đợi Eureka khởi động xong
3. **Auth Service** (8081)
4. **Product Service** (8082)
5. **Order Service** (8083)
6. **Inventory Service** (8084)
7. **Frontend** (3000)

## ✅ Kiểm tra kết nối

Sau khi tất cả services đã chạy, kiểm tra:
- Eureka Dashboard hiển thị tất cả services: http://localhost:8761
- API Gateway health: http://localhost:8080/actuator/health
- Frontend có thể gọi API qua Gateway

