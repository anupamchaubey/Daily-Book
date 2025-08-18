package com.DailyBook.repository;

import com.DailyBook.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByUsername(String username);

    // Search users by partial username
    List<UserProfile> findByUsernameContainingIgnoreCase(String username);
}
