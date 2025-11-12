package com.ecomerce.notificationservice.controller;

import com.ecomerce.notificationservice.annontation.ApiMessage;
import com.ecomerce.notificationservice.dto.response.NotificationResponse;
import com.ecomerce.notificationservice.dto.response.PageResponseDto;
import com.ecomerce.notificationservice.service.NotificationService;
import com.ecomerce.notificationservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "Quản lý thông báo")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @ApiMessage(value = "Lấy danh sách thông báo thành công")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy tất cả thông báo của user với pagination")
    public ResponseEntity<PageResponseDto<NotificationResponse>> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        PageResponseDto<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @ApiMessage(value = "Lấy thông báo chưa đọc thành công")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy thông báo chưa đọc")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        Long userId = JwtUtil.getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    @ApiMessage(value = "Lấy số lượng thông báo chưa đọc thành công")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = JwtUtil.getCurrentUserId();
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    @ApiMessage(value = "Đánh dấu thông báo đã đọc thành công")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đánh dấu thông báo đã đọc")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = JwtUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @ApiMessage(value = "Đánh dấu tất cả thông báo đã đọc thành công")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = JwtUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
