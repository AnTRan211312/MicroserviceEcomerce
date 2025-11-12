package com.ecomerce.paymentservice.controller;

import com.ecomerce.paymentservice.dto.request.PaymentCreateRequest;
import com.ecomerce.paymentservice.dto.response.ApiResponse;
import com.ecomerce.paymentservice.dto.response.PaymentCreateResponse;
import com.ecomerce.paymentservice.dto.response.PaymentResponse;
import com.ecomerce.paymentservice.dto.response.PageResponseDto;
import com.ecomerce.paymentservice.model.Payment;
import com.ecomerce.paymentservice.service.PaymentService;
import com.ecomerce.paymentservice.util.JwtUtil;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payment", description = "Quản lý thanh toán")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    // ==================== USER ENDPOINTS (Chỉ cần authentication, không cần @PreAuthorize) ====================
    
    @PostMapping("/create")
    @Operation(summary = "Tạo thanh toán", description = "Tạo thanh toán VNPay hoặc COD cho đơn hàng. VNPay: trả về payment URL. COD: tự động xác nhận ngay.")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPayment(
            @Valid @RequestBody PaymentCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        PaymentCreateResponse response;
        
        // For COD, HttpServletRequest is optional
        if ("COD".equals(request.getPaymentMethod()) || 
            (request.getPaymentMethod() == null || request.getPaymentMethod().isEmpty())) {
            response = paymentService.createPaymentWithoutRequest(userId, request);
        } else {
            response = paymentService.createPayment(userId, request, httpRequest);
        }
        
        return ResponseEntity.ok(new ApiResponse<>("Tạo thanh toán thành công", null, response));
    }
    
    @PostMapping("/cod/{orderId}/complete")
    @Operation(summary = "Hoàn tất thanh toán COD", description = "Đánh dấu thanh toán COD đã hoàn tất (khi đơn hàng được giao). COD payment thường đã SUCCESS khi tạo, endpoint này dùng để xác nhận lại khi cần.")
    public ResponseEntity<ApiResponse<PaymentResponse>> completeCodPayment(@PathVariable Long orderId) {
        Long userId = JwtUtil.getCurrentUserId();
        PaymentResponse response = paymentService.completeCodPayment(orderId, userId);
        return ResponseEntity.ok(new ApiResponse<>("Hoàn tất thanh toán COD thành công", null, response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin thanh toán theo ID", description = "Lấy thông tin thanh toán của user hiện tại")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        Long userId = JwtUtil.getCurrentUserId();
        PaymentResponse response = paymentService.getPaymentById(id, userId);
        return ResponseEntity.ok(new ApiResponse<>("Lấy thông tin thanh toán thành công", null, response));
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Lấy thông tin thanh toán theo Order ID", description = "Lấy thông tin thanh toán của order thuộc user hiện tại")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        Long userId = JwtUtil.getCurrentUserId();
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, userId);
        return ResponseEntity.ok(new ApiResponse<>("Lấy thông tin thanh toán thành công", null, response));
    }
    
    @GetMapping("/my-payments")
    @Operation(summary = "Lấy danh sách thanh toán của user", description = "Lấy tất cả thanh toán của user hiện tại")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments() {
        Long userId = JwtUtil.getCurrentUserId();
        List<PaymentResponse> responses = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách thanh toán thành công", null, responses));
    }
    
    // ==================== PUBLIC ENDPOINT (VNPay callback) ====================
    
    @GetMapping("/vnpay-callback")
    @Operation(summary = "Callback từ VNPay", description = "Xử lý callback từ VNPay sau khi thanh toán (Public endpoint)")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnpayCallback(HttpServletRequest request) {
        PaymentResponse response = paymentService.handleVnpayCallback(request);
        return ResponseEntity.ok(new ApiResponse<>("Xử lý callback thành công", null, response));
    }
    
    // ==================== ADMIN ENDPOINTS (Cần @PreAuthorize với permission) ====================
    
    @GetMapping
    @PreAuthorize("hasAuthority('GET /api/payments')")
    @Operation(
            summary = "Lấy danh sách tất cả thanh toán cho admin với filtering",
            description = "Yêu cầu quyền: <b>GET /api/payments</b>. Hỗ trợ filtering qua query string. Ví dụ: ?userId=1&status=SUCCESS&amount>100000&createdAt>2024-01-01&page=0&size=20&sort=createdAt,desc"
    )
    public ResponseEntity<ApiResponse<PageResponseDto<PaymentResponse>>> getAllPayments(
            @Filter Specification<Payment> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDto<PaymentResponse> payments = paymentService.getAllPayments(spec, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách thanh toán thành công", null, payments));
    }
}

