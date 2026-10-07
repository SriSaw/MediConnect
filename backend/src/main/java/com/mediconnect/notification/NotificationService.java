package com.mediconnect.notification;

import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.ForbiddenException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(UserPrincipal currentUser, Pageable pageable) {
        return PageResponse.from(notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(NotificationResponse::from));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal currentUser) {
        return notificationRepository.countByUserIdAndReadFalse(currentUser.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(UserPrincipal currentUser, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot modify another user's notification");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.from(saved);
    }

    @Transactional
    public void markAllAsRead(UserPrincipal currentUser) {
        notificationRepository.markAllAsReadForUser(currentUser.getId());
    }

    @Transactional
    public void createNotification(User user, String title, String message, NotificationType type) {
        try {
            Notification notification = new Notification(user, title, message, type);
            notificationRepository.save(notification);
            log.info("Created notification for user {}: {}", user.getId(), title);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", user.getId(), e.getMessage());
        }
    }
}
