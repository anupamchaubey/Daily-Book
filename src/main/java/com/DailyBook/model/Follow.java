// com/DailyBook/model/Follow.java
package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "follows")
public class Follow {
    @Id
    private String id;

    private String followerId;  // who follows
    private String followingId; // who is being followed

    private Instant createdAt;
}
