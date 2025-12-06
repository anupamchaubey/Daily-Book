package com.DailyBook.repository;

import com.DailyBook.model.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FollowRepository extends MongoRepository<Follow, String> {

    boolean existsByFollowerUsernameAndFolloweeUsername(
            String followerUsername,
            String followeeUsername
    );

    boolean existsByFollowerUsernameAndFolloweeUsernameAndStatus(
            String followerUsername,
            String followeeUsername,
            Follow.Status status
    );

    void deleteByFollowerUsernameAndFolloweeUsername(
            String followerUsername,
            String followeeUsername
    );

    List<Follow> findByFollowerUsernameAndStatus(
            String followerUsername,
            Follow.Status status
    );



    List<Follow> findByFolloweeUsernameAndStatus(
            String followeeUsername,
            Follow.Status status
    );

    Follow findByFollowerUsernameAndFolloweeUsername(
            String followerUsername,
            String followeeUsername
    );
}
