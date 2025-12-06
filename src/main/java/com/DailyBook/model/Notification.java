package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    // who should see this notification
    private String recipientUsername;

    // who triggered it (follower etc.)
    private String actorUsername;

    // type of notification
    private Type type;

    // optional extra data (e.g. related username, entry id etc.)
    private String message;

    private boolean read;

    @CreatedDate
    private Instant createdAt;

    public enum Type {
        FOLLOW_REQUEST,
        FOLLOW_APPROVED
    }
}
