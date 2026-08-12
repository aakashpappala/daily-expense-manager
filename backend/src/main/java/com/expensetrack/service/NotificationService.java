package com.expensetrack.service;



import com.expensetrack.dto.NotificationDto;
import com.expensetrack.entity.Notification;
import com.expensetrack.entity.User;
import com.expensetrack.exception.ResourceNotFoundException;
import com.expensetrack.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final MailService mailService;

    public List<NotificationDto> getUserNotifications() {
        User user = userService.getAuthenticatedUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount() {
        User user = userService.getAuthenticatedUser();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional

    public void createNotification(User user, String message, String type) {

        Notification notification = Notification.builder()
                .message(message)
                .type(type)
                .isRead(false)
                .user(user)
                .build();

        notificationRepository.save(notification);

        mailService.sendNotificationEmail(
                user.getEmail(),
                message
        );
    }

    @Transactional
    public NotificationDto markAsRead(Long id) {
        User user = userService.getAuthenticatedUser();
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return mapToDto(notification);
    }

    @Transactional
    public void markAllAsRead() {
        User user = userService.getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
