package com.DailyBook.repository;

import com.DailyBook.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    Optional<UserProfile> findByUsername(String username);

    // 🔍 Public people search (username only, max 15)
    @Query("""
    {
      username: { $regex: ?0, $options: 'i' }
    }
    """)
    List<UserProfile> findTop15ByUsernameRegex(String query);
    @Query("{}")
    List<UserProfile> findTop15By();

}
