package com.DailyBook.repository;

import com.DailyBook.model.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FollowRepository extends MongoRepository<Follow, String> {

    boolean existsByFollowerUsernameAndFolloweeUsername(String followerUsername, String followeeUsername);

    void deleteByFollowerUsernameAndFolloweeUsername(String followerUsername, String followeeUsername);

    List<Follow> findByFollowerUsername(String followerUsername);

    List<Follow> findByFolloweeUsername(String followeeUsername);
}
