package com.DailyBook.service;

import com.DailyBook.dto.NotificationResponse;
import com.DailyBook.model.Notification;
import com.DailyBook.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ---- INTERNAL HELPERS ----

    public void createFollowRequestNotification(String follower, String followee) {
        Notification n = Notification.builder()
                .recipientUsername(followee)
                .actorUsername(follower)
                .type(Notification.Type.FOLLOW_REQUEST)
                .message(follower + " requested to follow you")
                .read(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(n);
    }

    public void createFollowApprovedNotification(String follower, String followee) {
        // follower = person who requested
        // followee = person who approved
        Notification n = Notification.builder()
                .recipientUsername(follower)
                .actorUsername(followee)
                .type(Notification.Type.FOLLOW_APPROVED)
                .message(followee + " approved your follow request")
                .read(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(n);
    }

    // ---- PUBLIC API FOR CONTROLLER ----

    public Page<NotificationResponse> getNotificationsForUser(
            String username,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        return notificationRepository
                .findByRecipientUsernameOrderByCreatedAtDesc(username, pageable)
                .map(this::toResponse);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndReadIsFalse(username);
    }

    public void markAsRead(String username, String notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!n.getRecipientUsername().equals(username)) {
            throw new RuntimeException("Not allowed");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }

    public void markAllAsRead(String username) {
        List<Notification> list = notificationRepository
                .findByRecipientUsernameOrderByCreatedAtDesc(username, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientUsername(n.getRecipientUsername())
                .actorUsername(n.getActorUsername())
                .type(n.getType())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
