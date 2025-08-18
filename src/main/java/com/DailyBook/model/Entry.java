package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entry {

    @Id
    private String id;

    private String userId;  // Owner of the entry
    private String title;
    private String content;

    private List<String> tags;

    private Visibility visibility = Visibility.PRIVATE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum Visibility {
        PUBLIC,       // everyone can see
        PRIVATE,      // only the owner
        FOLLOWERS_ONLY // only followers
    }

}
