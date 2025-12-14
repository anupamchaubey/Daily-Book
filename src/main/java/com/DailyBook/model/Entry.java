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
                name = "visibility_createdAt_idx",
                def = "{'visibility': 1, 'createdAt': -1}"
        ),
        @CompoundIndex(
                name = "entries_text_idx",
                def = "{'title': 'text', 'content': 'text', 'tags': 'text'}"
        )
})
public class Entry {

    @Id
    private String id;

    // you are storing username here
    private String userId;

    private String title;
    private String content;
    private List<String> tags;

    private Visibility visibility = Visibility.PRIVATE;

    // 🌆 multiple image URLs
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
