package com.mediconnect.notification;

import com.mediconnect.common.PageResponse;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "User notification endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get current user notifications")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser, pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(currentUser)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(currentUser, id));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.noContent().build();
    }
}
