package com.ecomerce.notificationservice.service.impl;

import com.ecomerce.notificationservice.dto.response.NotificationResponse;
import com.ecomerce.notificationservice.dto.response.PageResponseDto;
import com.ecomerce.notificationservice.event.CartItemAddedEvent;
import com.ecomerce.notificationservice.event.OrderCreatedEvent;
import com.ecomerce.notificationservice.event.OrderStatusChangedEvent;
import com.ecomerce.notificationservice.event.PaymentFailedEvent;
import com.ecomerce.notificationservice.event.PaymentSuccessEvent;
import com.ecomerce.notificationservice.model.Notification;
import com.ecomerce.notificationservice.repository.NotificationRepository;
import com.ecomerce.notificationservice.service.NotificationService;
import com.ecomerce.notificationservice.advice.exeption.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void sendCartItemAddedNotification(CartItemAddedEvent event) {
        try {
            // Format price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            String formattedPrice = currencyFormat.format(event.getProductPrice());
            String totalPrice = currencyFormat.format(
                    event.getProductPrice().multiply(BigDecimal.valueOf(event.getQuantity()))
            );

            // Create notification message
            String title = "✅ Đã thêm sản phẩm vào giỏ hàng";
            String message = String.format(
                    "Bạn đã thêm sản phẩm vào giỏ hàng thành công!\n\n" +
                    "📦 Thông tin sản phẩm:\n" +
                    "   - Tên sản phẩm: %s\n" +
                    "   - Giá: %s\n" +
                    "   - Số lượng: %d\n" +
                    "   - Tổng tiền: %s",
                    event.getProductName(),
                    formattedPrice,
                    event.getQuantity(),
                    totalPrice
            );

            // Save notification to database
            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .title(title)
                    .message(message)
                    .type("SUCCESS")
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("✅ Created notification for user: {}, product: {}", 
                    event.getUserId(), event.getProductName());

        } catch (Exception e) {
            log.error("❌ Error creating notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendOrderCreatedNotification(OrderCreatedEvent event) {
        try {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            String formattedTotal = currencyFormat.format(event.getTotalAmount());

            String title = "📦 Đơn hàng đã được tạo";
            String message = String.format(
                    "Đơn hàng của bạn đã được tạo thành công!\n\n" +
                    "📋 Thông tin đơn hàng:\n" +
                    "   - Mã đơn hàng: %s\n" +
                    "   - Tổng tiền: %s\n" +
                    "   - Số lượng sản phẩm: %d\n" +
                    "   - Địa chỉ giao hàng: %s\n" +
                    "   - Số điện thoại: %s\n\n" +
                    "Đơn hàng đang ở trạng thái: %s",
                    event.getOrderNumber(),
                    formattedTotal,
                    event.getItems() != null ? event.getItems().size() : 0,
                    event.getShippingAddress() != null ? event.getShippingAddress() : "N/A",
                    event.getPhone() != null ? event.getPhone() : "N/A",
                    event.getStatus()
            );

            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .title(title)
                    .message(message)
                    .type("INFO")
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("✅ Created order created notification for user: {}, order: {}", 
                    event.getUserId(), event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Error creating order created notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendOrderStatusChangedNotification(OrderStatusChangedEvent event) {
        try {
            String title;
            String message;
            String type;

            // Tạo thông báo dựa trên status mới
            switch (event.getNewStatus()) {
                case "CONFIRMED":
                    title = "✅ Đơn hàng đã được xác nhận";
                    message = String.format(
                            "Đơn hàng %s của bạn đã được xác nhận!\n\n" +
                            "Đơn hàng đang được chuẩn bị. Chúng tôi sẽ thông báo khi đơn hàng được giao.",
                            event.getOrderNumber()
                    );
                    type = "SUCCESS";
                    break;
                case "PROCESSING":
                    title = "🔄 Đơn hàng đang được xử lý";
                    message = String.format(
                            "Đơn hàng %s của bạn đang được xử lý.\n\n" +
                            "Chúng tôi đang chuẩn bị hàng để giao cho bạn.",
                            event.getOrderNumber()
                    );
                    type = "INFO";
                    break;
                case "SHIPPED":
                    title = "🚚 Đơn hàng đã được giao hàng";
                    message = String.format(
                            "Đơn hàng %s của bạn đã được giao hàng!\n\n" +
                            "Đơn hàng đang trên đường đến bạn. Vui lòng chờ nhận hàng.",
                            event.getOrderNumber()
                    );
                    type = "INFO";
                    break;
                case "DELIVERED":
                    title = "🎉 Đơn hàng đã được giao thành công";
                    message = String.format(
                            "Đơn hàng %s của bạn đã được giao thành công!\n\n" +
                            "Cảm ơn bạn đã mua sắm. Hãy đánh giá sản phẩm để giúp chúng tôi cải thiện dịch vụ.",
                            event.getOrderNumber()
                    );
                    type = "SUCCESS";
                    break;
                case "CANCELLED":
                    title = "❌ Đơn hàng đã bị hủy";
                    message = String.format(
                            "Đơn hàng %s của bạn đã bị hủy.\n\n" +
                            "Nếu bạn đã thanh toán, tiền sẽ được hoàn lại trong vòng 3-5 ngày làm việc.",
                            event.getOrderNumber()
                    );
                    type = "WARNING";
                    break;
                default:
                    title = "ℹ️ Trạng thái đơn hàng thay đổi";
                    message = String.format(
                            "Trạng thái đơn hàng %s đã thay đổi từ %s sang %s.",
                            event.getOrderNumber(),
                            event.getOldStatus(),
                            event.getNewStatus()
                    );
                    type = "INFO";
            }

            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("✅ Created order status changed notification for user: {}, order: {}, status: {} -> {}", 
                    event.getUserId(), event.getOrderNumber(), event.getOldStatus(), event.getNewStatus());
        } catch (Exception e) {
            log.error("❌ Error creating order status changed notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendPaymentSuccessNotification(PaymentSuccessEvent event) {
        try {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            String formattedAmount = currencyFormat.format(event.getAmount());

            String title = "✅ Thanh toán thành công";
            String message = String.format(
                    "Thanh toán cho đơn hàng đã thành công!\n\n" +
                    "💳 Thông tin thanh toán:\n" +
                    "   - Mã thanh toán: %d\n" +
                    "   - Mã đơn hàng: %s\n" +
                    "   - Số tiền: %s\n" +
                    "   - Phương thức: %s\n" +
                    "   - Mã giao dịch: %s\n\n" +
                    "Đơn hàng của bạn đã được xác nhận và đang được xử lý.",
                    event.getPaymentId(),
                    "ORDER-" + event.getOrderId(), // Có thể cần orderNumber từ order service
                    formattedAmount,
                    event.getPaymentMethod() != null ? event.getPaymentMethod() : "N/A",
                    event.getVnpayTransactionNo() != null ? event.getVnpayTransactionNo() : "N/A"
            );

            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .title(title)
                    .message(message)
                    .type("SUCCESS")
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("✅ Created payment success notification for user: {}, payment: {}", 
                    event.getUserId(), event.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Error creating payment success notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendPaymentFailedNotification(PaymentFailedEvent event) {
        try {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            String formattedAmount = currencyFormat.format(event.getAmount());

            String title = "❌ Thanh toán thất bại";
            String message = String.format(
                    "Thanh toán cho đơn hàng không thành công.\n\n" +
                    "💳 Thông tin thanh toán:\n" +
                    "   - Mã thanh toán: %d\n" +
                    "   - Số tiền: %s\n" +
                    "   - Phương thức: %s\n" +
                    "   - Lý do: %s\n\n" +
                    "Vui lòng thử lại hoặc liên hệ hỗ trợ nếu vấn đề vẫn tiếp tục.",
                    event.getPaymentId(),
                    formattedAmount,
                    event.getPaymentMethod() != null ? event.getPaymentMethod() : "N/A",
                    event.getFailureReason() != null ? event.getFailureReason() : "Không xác định"
            );

            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .title(title)
                    .message(message)
                    .type("ERROR")
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("✅ Created payment failed notification for user: {}, payment: {}", 
                    event.getUserId(), event.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Error creating payment failed notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));

        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Thông báo không thuộc về user này");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("✅ Marked notification {} as read for user: {}", notificationId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("✅ Marked {} notifications as read for user: {}", 
                unreadNotifications.size(), userId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

