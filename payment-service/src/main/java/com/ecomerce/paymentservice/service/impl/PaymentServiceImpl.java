package com.ecomerce.paymentservice.service.impl;

import com.ecomerce.paymentservice.client.OrderServiceClient;
import com.ecomerce.paymentservice.client.dto.OrderInfo;
import com.ecomerce.paymentservice.config.VnpayConfig;
import com.ecomerce.paymentservice.dto.request.PaymentCreateRequest;
import com.ecomerce.paymentservice.dto.response.PaymentCreateResponse;
import com.ecomerce.paymentservice.dto.response.PaymentResponse;
import com.ecomerce.paymentservice.dto.response.PageResponseDto;
import com.ecomerce.paymentservice.event.PaymentCreatedEvent;
import com.ecomerce.paymentservice.event.PaymentFailedEvent;
import com.ecomerce.paymentservice.event.PaymentSuccessEvent;
import com.ecomerce.paymentservice.model.Payment;
import com.ecomerce.paymentservice.model.PaymentMethod;
import com.ecomerce.paymentservice.repository.PaymentRepository;
import com.ecomerce.paymentservice.service.KafkaProducerService;
import com.ecomerce.paymentservice.service.PaymentService;
import com.ecomerce.paymentservice.util.VnpayUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final VnpayConfig vnpayConfig;
    private final VnpayUtil vnpayUtil;
    private final ObjectMapper objectMapper;
    private final KafkaProducerService kafkaProducerService;
    private final OrderServiceClient orderServiceClient;
    
    @Value("${vnpay.return-url}")
    private String returnUrl;
    
    @Override
    @Transactional
    public PaymentCreateResponse createPaymentWithoutRequest(Long userId, PaymentCreateRequest request) {
        // For COD, HttpServletRequest is not needed
        // For VNPay, we'll use a default IP or extract from context if available
        // This method is mainly for Feign clients
        return createPaymentInternal(userId, request, null);
    }
    
    @Override
    @Transactional
    public PaymentCreateResponse createPayment(Long userId, PaymentCreateRequest request, HttpServletRequest httpRequest) {
        return createPaymentInternal(userId, request, httpRequest);
    }
    
    @Transactional
    private PaymentCreateResponse createPaymentInternal(Long userId, PaymentCreateRequest request, HttpServletRequest httpRequest) {
        // Get order info from order-service
        OrderInfo orderInfo;
        try {
            orderInfo = orderServiceClient.getOrderById(request.getOrderId());
            if (orderInfo == null) {
                throw new RuntimeException("Order không tồn tại với ID: " + request.getOrderId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to get order from order-service: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin đơn hàng: " + e.getMessage());
        }
        
        // Validate order belongs to user
        if (orderInfo.getUserId() == null || !orderInfo.getUserId().equals(userId)) {
            throw new RuntimeException("Order không thuộc về user này");
        }
        
        // Validate order status - chỉ cho phép thanh toán order PENDING
        if (orderInfo.getStatus() == null || !orderInfo.getStatus().equals("PENDING")) {
            throw new RuntimeException(
                String.format("Chỉ có thể thanh toán cho đơn hàng ở trạng thái PENDING. Trạng thái hiện tại: %s", 
                    orderInfo.getStatus())
            );
        }
        
        // Validate order amount
        if (orderInfo.getTotalAmount() == null || orderInfo.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền đơn hàng không hợp lệ: " + orderInfo.getTotalAmount());
        }
        
        // Check if order already has successful payment
        List<Payment> existingPayments = paymentRepository.findByOrderId(request.getOrderId());
        boolean hasSuccessfulPayment = existingPayments.stream()
                .anyMatch(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS);
        
        if (hasSuccessfulPayment) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán thành công");
        }
        
        // Get payment method (default to VNPAY)
        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = PaymentMethod.getDefault();
        }
        
        // Validate payment method
        if (!PaymentMethod.isValid(paymentMethod)) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ: " + paymentMethod);
        }
        
        log.info("✅ Verified order: {} for user: {}, totalAmount: {}, paymentMethod: {}", 
                request.getOrderId(), userId, orderInfo.getTotalAmount(), paymentMethod);
        
        // Handle COD payment
        if (PaymentMethod.COD.equals(paymentMethod)) {
            return createCodPayment(userId, request, orderInfo);
        }
        
        // Handle VNPay payment (existing flow)
        // VNPay requires HttpServletRequest for IP address
        if (httpRequest == null) {
            throw new RuntimeException("VNPay payment requires HttpServletRequest for IP address");
        }
        return createVnpayPayment(userId, request, orderInfo, httpRequest);
    }
    
    /**
     * Create COD payment - Auto-confirmed immediately
     */
    private PaymentCreateResponse createCodPayment(Long userId, PaymentCreateRequest request, OrderInfo orderInfo) {
        log.info("💵 Creating COD payment for order: {}, user: {}", request.getOrderId(), userId);
        
        // Tạo payment record với status SUCCESS ngay (COD = accepted)
        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(request.getOrderId())
                .amount(orderInfo.getTotalAmount())
                .status(Payment.PaymentStatus.SUCCESS) // COD payment is auto-confirmed
                .paymentMethod(PaymentMethod.COD)
                .build();
        
        payment = paymentRepository.save(payment);
        log.info("✅ Created COD payment: {} for order: {}", payment.getId(), request.getOrderId());
        
        // Publish PaymentCreatedEvent (for tracking)
        PaymentCreatedEvent createdEvent = PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .vnpayTxnRef(null) // COD doesn't have VNPay transaction
                .paymentUrl(null) // COD doesn't have payment URL
                .timestamp(Instant.now())
                .build();
        kafkaProducerService.publishPaymentCreatedEvent(createdEvent);
        
        // Publish PaymentSuccessEvent immediately (to confirm order)
        PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .vnpayTxnRef(null)
                .vnpayTransactionNo(null)
                .vnpayResponseCode(null)
                .timestamp(Instant.now())
                .build();
        
        kafkaProducerService.publishPaymentSuccessEvent(successEvent);
        log.info("✅ Published PaymentSuccessEvent for COD payment: {}", payment.getId());
        
        return PaymentCreateResponse.builder()
                .paymentUrl(null) // COD doesn't have payment URL
                .paymentId(payment.getId())
                .message("Thanh toán COD đã được tạo thành công. Đơn hàng sẽ được xác nhận ngay.")
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(Payment.PaymentStatus.SUCCESS.name())
                .build();
    }
    
    /**
     * Create VNPay payment - Generate payment URL
     */
    private PaymentCreateResponse createVnpayPayment(Long userId, PaymentCreateRequest request, OrderInfo orderInfo, HttpServletRequest httpRequest) {
        log.info("💳 Creating VNPay payment for order: {}, user: {}", request.getOrderId(), userId);
        
        // Tạo payment record với amount từ order
        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(request.getOrderId())
                .amount(orderInfo.getTotalAmount())
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.VNPAY)
                .build();
        
        payment = paymentRepository.save(payment);
        
        // Tạo mã giao dịch VNPay (vnp_TxnRef)
        String vnp_TxnRef = "ORDER" + payment.getId() + System.currentTimeMillis();
        payment.setVnpayTxnRef(vnp_TxnRef);
        payment = paymentRepository.save(payment);
        
        // Tạo URL thanh toán VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpayConfig.getVersion());
        vnpParams.put("vnp_Command", vnpayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(payment.getAmount().longValue() * 100)); // VNPay yêu cầu số tiền * 100
        vnpParams.put("vnp_CurrCode", vnpayConfig.getCurrCode());
        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderDescription() != null 
            ? request.getOrderDescription() 
            : "Thanh toan don hang #" + request.getOrderId());
        vnpParams.put("vnp_OrderType", vnpayConfig.getOrderType());
        vnpParams.put("vnp_Locale", vnpayConfig.getLocale());
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", vnpayUtil.getIpAddress(httpRequest));
        vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        
        String paymentUrl = vnpayUtil.createPaymentUrl(vnpParams, vnpayConfig.getPaymentUrl());
        
        log.info("✅ Created VNPay payment URL for payment ID: {}, Order ID: {}", payment.getId(), request.getOrderId());
        
        // Publish PaymentCreatedEvent to Kafka
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .vnpayTxnRef(payment.getVnpayTxnRef())
                .paymentUrl(paymentUrl)
                .timestamp(Instant.now())
                .build();
        
        kafkaProducerService.publishPaymentCreatedEvent(event);
        
        return PaymentCreateResponse.builder()
                .paymentUrl(paymentUrl)
                .paymentId(payment.getId())
                .message("Tạo URL thanh toán VNPay thành công")
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(Payment.PaymentStatus.PENDING.name())
                .build();
    }
    
    @Override
    @Transactional
    public PaymentResponse handleVnpayCallback(HttpServletRequest request) {
        Map<String, String> vnpParams = new HashMap<>();
        
        // Lấy tất cả parameters từ request
        for (String paramName : Collections.list(request.getParameterNames())) {
            if (paramName.startsWith("vnp_")) {
                vnpParams.put(paramName, request.getParameter(paramName));
            }
        }
        
        // Validate chữ ký
        Map<String, String> paramsCopy = new HashMap<>(vnpParams);
        if (!vnpayUtil.validateCallback(paramsCopy)) {
            log.error("Invalid VNPay callback signature");
            throw new RuntimeException("Invalid VNPay callback signature");
        }
        
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnp_TransactionStatus = vnpParams.get("vnp_TransactionStatus");
        
        Payment payment = paymentRepository.findByVnpayTxnRef(vnp_TxnRef)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + vnp_TxnRef));
        
        // Lưu callback data
        try {
            payment.setCallbackData(objectMapper.writeValueAsString(vnpParams));
        } catch (Exception e) {
            log.error("Error saving callback data", e);
        }
        
        payment.setVnpayTransactionNo(vnp_TransactionNo);
        payment.setVnpayResponseCode(vnp_ResponseCode);
        
        // Cập nhật trạng thái thanh toán
        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setVnpayMessage("Giao dịch thành công");
            log.info("Payment successful: {}", vnp_TxnRef);
            
            // Publish PaymentSuccessEvent to Kafka
            PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .vnpayTxnRef(payment.getVnpayTxnRef())
                    .vnpayTransactionNo(payment.getVnpayTransactionNo())
                    .vnpayResponseCode(payment.getVnpayResponseCode())
                    .timestamp(Instant.now())
                    .build();
            
            kafkaProducerService.publishPaymentSuccessEvent(successEvent);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setVnpayMessage("Giao dịch thất bại: " + vnpParams.get("vnp_ResponseMessage"));
            log.warn("Payment failed: {}, ResponseCode: {}", vnp_TxnRef, vnp_ResponseCode);
            
            // Publish PaymentFailedEvent to Kafka
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .vnpayTxnRef(payment.getVnpayTxnRef())
                    .vnpayResponseCode(payment.getVnpayResponseCode())
                    .failureReason(vnpParams.get("vnp_ResponseMessage"))
                    .timestamp(Instant.now())
                    .build();
            
            kafkaProducerService.publishPaymentFailedEvent(failedEvent);
        }
        
        payment = paymentRepository.save(payment);
        
        return mapToResponse(payment);
    }
    
    @Override
    public PaymentResponse getPaymentById(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to payment");
        }
        
        return mapToResponse(payment);
    }
    
    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .stream()
            .filter(p -> p.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        
        return mapToResponse(payment);
    }
    
    @Override
    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public PageResponseDto<PaymentResponse> getAllPayments(Specification<Payment> spec, Pageable pageable) {
        Page<Payment> paymentPage = paymentRepository.findAll(spec, pageable);
        List<PaymentResponse> paymentResponses = paymentPage.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponseDto<>(
            paymentResponses,
            pageable.getPageNumber() + 1,
            pageable.getPageSize(),
            paymentPage.getTotalElements(),
            paymentPage.getTotalPages()
        );
    }
    
    @Override
    @Transactional
    public PaymentResponse completeCodPayment(Long orderId, Long userId) {
        log.info("💵 Completing COD payment for order: {}, user: {}", orderId, userId);
        
        // Find payment by orderId
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        Payment payment = payments.stream()
                .filter(p -> p.getUserId().equals(userId))
                .filter(p -> PaymentMethod.COD.equals(p.getPaymentMethod()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment COD cho order: " + orderId));
        
        // Validate payment belongs to user
        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Payment không thuộc về user này");
        }
        
        // Validate payment method is COD
        if (!PaymentMethod.COD.equals(payment.getPaymentMethod())) {
            throw new RuntimeException("Payment này không phải COD payment");
        }
        
        // COD payment is already SUCCESS when created, but if needed to mark as completed again
        // This method can be used when order is delivered (optional flow)
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            log.info("COD payment {} is already SUCCESS, no action needed", payment.getId());
            return mapToResponse(payment);
        }
        
        // Update payment status to SUCCESS (if it was PENDING for some reason)
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment = paymentRepository.save(payment);
        
        // Publish PaymentSuccessEvent (if not already published)
        PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .vnpayTxnRef(null)
                .vnpayTransactionNo(null)
                .vnpayResponseCode(null)
                .timestamp(Instant.now())
                .build();
        
        kafkaProducerService.publishPaymentSuccessEvent(successEvent);
        log.info("✅ Completed COD payment: {} for order: {}", payment.getId(), orderId);
        
        return mapToResponse(payment);
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .orderId(payment.getOrderId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .status(payment.getStatus())
            .paymentMethod(payment.getPaymentMethod())
            .vnpayTxnRef(payment.getVnpayTxnRef())
            .vnpayTransactionNo(payment.getVnpayTransactionNo())
            .vnpayResponseCode(payment.getVnpayResponseCode())
            .vnpayMessage(payment.getVnpayMessage())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }
}

