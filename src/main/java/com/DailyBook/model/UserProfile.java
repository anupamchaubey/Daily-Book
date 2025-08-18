package com.DailyBook.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    private String id;  // same as User.id (for easy mapping)

    private String username;   // display name
    private String bio;        // about user
    private String profilePicture; // URL or Base64 (for now simple string)

    private Instant joinedAt;
}
