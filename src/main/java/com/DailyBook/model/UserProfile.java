package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(
                name = "user_search_idx",
                def = "{'username': 1}"
        )
})
public class UserProfile {
    @Id
    private String id;

    private String username;
    private String bio;
    private String profilePicture;

    private Instant joinedAt;
}
