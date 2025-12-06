package com.DailyBook.dto;

import com.DailyBook.model.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {

    private String id;
    private String recipientUsername;
    private String actorUsername;
    private Notification.Type type;
    private String message;
    private boolean read;
    private Instant createdAt;
}
