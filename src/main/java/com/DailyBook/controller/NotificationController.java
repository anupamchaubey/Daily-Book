package com.DailyBook.controller;

import com.DailyBook.dto.NotificationResponse;
import com.DailyBook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET all notifications for current user
    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String me = authentication.getName();
        return notificationService.getNotificationsForUser(me, page, size);
    }

    // GET unread count (for showing badge)
    @GetMapping("/unread-count")
    public long getUnreadCount(Authentication authentication) {
        String me = authentication.getName();
        return notificationService.getUnreadCount(me);
    }

    // Mark a single notification as read
    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable String id, Authentication authentication) {
        String me = authentication.getName();
        notificationService.markAsRead(me, id);
    }

    // Mark all as read
    @PostMapping("/read-all")
    public void markAllAsRead(Authentication authentication) {
        String me = authentication.getName();
        notificationService.markAllAsRead(me);
    }
}
