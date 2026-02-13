package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "follows")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(
        name = "unique_follow",
        def = "{'followerUsername':1,'followeeUsername':1}",
        unique = true
)


public class Follow {

    @Id
    private String id;

    private String followerUsername;
    private String followeeUsername;

    // NEW FIELD
    private Status status;

    private Instant createdAt;

    public enum Status {
        PENDING,
        APPROVED
    }
}
