# Hướng dẫn thiết lập Monitoring với Prometheus

## Tổng quan

Dashboard giám sát dịch vụ đã được tích hợp với **Prometheus** và **Spring Boot Actuator** để lấy dữ liệu thực tế từ các microservices.

## Cấu hình

### 1. Prometheus

Prometheus cần được chạy trên port `9090` và cấu hình scrape metrics từ các services qua endpoint `/actuator/prometheus`.

File cấu hình: `prometheus/prometheus.yml`

### 2. Environment Variables

Thêm vào file `.env` (nếu cần):

```env
VITE_PROMETHEUS_URL=http://localhost:9090
```

Trong development, hệ thống sẽ tự động sử dụng proxy `/prometheus` đã được cấu hình trong `vite.config.ts`.

### 3. Service Configuration

Các services cần có cấu hình sau trong `application.properties`:

```properties
# Management port
management.server.port=9091

# Expose Prometheus endpoint
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.prometheus.metrics.export.enabled=true
```

### 4. Metrics được thu thập

- **Total Requests**: Tổng số HTTP requests trong khoảng thời gian
- **Error Count**: Số lượng lỗi 5xx
- **Average Response Time**: Thời gian phản hồi trung bình (ms)
- **Uptime Percentage**: Tỷ lệ uptime dựa trên process uptime hoặc success rate
- **Health Status**: Trạng thái từ Spring Boot Actuator `/actuator/health`

## Cách hoạt động

1. **Prometheus Queries**: Sử dụng PromQL để query metrics:
   - `http_server_requests_seconds_count`: Tổng số requests
   - `http_server_requests_seconds_sum`: Tổng thời gian response
   - `process_uptime_seconds`: Thời gian uptime của process

2. **Health Checks**: Query trực tiếp đến management port của mỗi service:
   - `http://localhost:{managementPort}/actuator/health`

3. **Status Determination**:
   - **Healthy**: Uptime >= 99%, Error rate < 1%, Response time < 200ms
   - **Degraded**: Uptime >= 95%, Error rate < 5%
   - **Down**: Health status = DOWN hoặc không thể kết nối

## Fallback

Nếu không thể kết nối đến Prometheus, hệ thống sẽ:
1. Hiển thị cảnh báo
2. Sử dụng dữ liệu mẫu (mock data) để demo
3. Vẫn có thể refresh để thử lại

## Troubleshooting

### Prometheus không kết nối được

1. Kiểm tra Prometheus đang chạy: `http://localhost:9090`
2. Kiểm tra proxy trong `vite.config.ts`
3. Kiểm tra CORS nếu query trực tiếp

### Không có metrics

1. Kiểm tra services có expose `/actuator/prometheus` không
2. Kiểm tra Prometheus có scrape được services không
3. Kiểm tra label `application` hoặc `service` trong Prometheus metrics

### Health check timeout

1. Kiểm tra management port của service
2. Kiểm tra service có đang chạy không
3. Kiểm tra firewall/network

