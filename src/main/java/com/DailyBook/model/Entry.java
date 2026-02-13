package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(
                name = "feed_idx",
                def = "{'userId':1, 'visibility':1, 'createdAt':-1}"
        )
})

public class Entry {

    @Id
    private String id;

    private String userId;

    private String title;
    private String content;
    private List<String> tags;

    private Visibility visibility = Visibility.PRIVATE;

    private List<String> imageUrls;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum Visibility {
        PUBLIC,
        PRIVATE,
        FOLLOWERS_ONLY
    }
}
