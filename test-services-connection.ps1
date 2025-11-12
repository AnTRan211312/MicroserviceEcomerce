# Script kiểm tra kết nối giữa các services
# Chạy script này sau khi tất cả services đã start

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "KIỂM TRA KẾT NỐI GIỮA CÁC SERVICES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Kiểm tra Eureka Server
Write-Host "1. Kiểm tra Eureka Server..." -ForegroundColor Yellow
try {
    $eurekaResponse = Invoke-WebRequest -Uri "http://localhost:8761" -UseBasicParsing -TimeoutSec 5
    if ($eurekaResponse.StatusCode -eq 200) {
        Write-Host "   ✅ Eureka Server đang chạy tại http://localhost:8761" -ForegroundColor Green
    }
} catch {
    Write-Host "   ❌ Eureka Server không chạy hoặc không thể kết nối" -ForegroundColor Red
    Write-Host "   Hãy đảm bảo Eureka Server đang chạy tại port 8761" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 2. Kiểm tra các services đã đăng ký với Eureka
Write-Host "2. Kiểm tra services đã đăng ký với Eureka..." -ForegroundColor Yellow
$services = @("CART-SERVICE", "ORDER-SERVICE", "PAYMENT-SERVICE", "PRODUCT-SERVICE", "INVENTORY-SERVICE")
$registeredServices = @()

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps/$service" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "   ✅ $service đã đăng ký với Eureka" -ForegroundColor Green
            $registeredServices += $service
        }
    } catch {
        Write-Host "   ⚠️  $service chưa đăng ký với Eureka" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "   Tổng số services đã đăng ký: $($registeredServices.Count)/$($services.Count)" -ForegroundColor Cyan

if ($registeredServices.Count -eq 0) {
    Write-Host "   ❌ Không có service nào đăng ký với Eureka!" -ForegroundColor Red
    Write-Host "   Hãy đảm bảo các services đã start và có cấu hình Eureka đúng" -ForegroundColor Yellow
}

Write-Host ""

# 3. Kiểm tra health endpoints của các services
Write-Host "3. Kiểm tra health endpoints..." -ForegroundColor Yellow

$servicePorts = @{
    "CART-SERVICE" = 8083
    "ORDER-SERVICE" = 8084
    "PAYMENT-SERVICE" = 8085
    "PRODUCT-SERVICE" = 8082
    "INVENTORY-SERVICE" = 8086
}

foreach ($service in $services) {
    $port = $servicePorts[$service]
    if ($port) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Host "   ✅ $service health check OK (port $port)" -ForegroundColor Green
            }
        } catch {
            Write-Host "   ⚠️  $service không phản hồi tại port $port" -ForegroundColor Yellow
        }
    }
}

Write-Host ""

# 4. Test Feign Client connection (nếu có API Gateway)
Write-Host "4. Kiểm tra API Gateway..." -ForegroundColor Yellow
try {
    $gatewayResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($gatewayResponse.StatusCode -eq 200) {
        Write-Host "   ✅ API Gateway đang chạy tại http://localhost:8080" -ForegroundColor Green
    }
} catch {
    Write-Host "   ⚠️  API Gateway không chạy hoặc không thể kết nối" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "KẾT QUẢ KIỂM TRA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Nếu tất cả services đã đăng ký với Eureka:" -ForegroundColor Green
Write-Host "  - Feign Clients sẽ tự động resolve service names từ Eureka" -ForegroundColor White
Write-Host "  - LoadBalancer sẽ tự động load balance requests" -ForegroundColor White
Write-Host ""
Write-Host "Nếu vẫn gặp lỗi UnknownHostException:" -ForegroundColor Yellow
Write-Host "  1. Đảm bảo tất cả services đã start và đăng ký với Eureka" -ForegroundColor White
Write-Host "  2. Kiểm tra Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "  3. Đợi 10-30 giây để services đăng ký hoàn tất" -ForegroundColor White
Write-Host "  4. Kiểm tra logs của services để xem có lỗi gì không" -ForegroundColor White
Write-Host ""

