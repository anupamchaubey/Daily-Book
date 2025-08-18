package com.DailyBook.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserProfileResponse {
    private String id;
    private String username;
    private String bio;
    private String profilePicture;
    private Instant joinedAt;
}
