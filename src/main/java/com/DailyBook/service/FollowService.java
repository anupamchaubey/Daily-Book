package com.DailyBook.service;

import com.DailyBook.model.Follow;
import com.DailyBook.repository.FollowRepository;
import com.DailyBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;   // 👈 add this

// ------------ SEND FOLLOW REQUEST ------------
    public void follow(String follower, String followee) {

        if (follower.equals(followee))
            throw new IllegalArgumentException("You cannot follow yourself");

        if (!userRepository.existsByUsername(followee))
            throw new IllegalArgumentException("User not found");

        if (followRepository.existsByFollowerUsernameAndFolloweeUsername(follower, followee))
            return;

        Follow follow = Follow.builder()
                .followerUsername(follower)
                .followeeUsername(followee)
                .status(Follow.Status.PENDING)   // default = PENDING
                .createdAt(Instant.now())
                .build();

        followRepository.save(follow);

        notificationService.createFollowRequestNotification(follower, followee);
    }

    // ------------ APPROVE REQUEST ------------
    public void approveFollow(String me, String follower) {

        Follow follow =
                followRepository.findByFollowerUsernameAndFolloweeUsername(follower, me);

        if (follow == null)
            throw new RuntimeException("Request not found");

        follow.setStatus(Follow.Status.APPROVED);
        followRepository.save(follow);

        notificationService.createFollowApprovedNotification(follower, me);

    }

    // ------------ REJECT REQUEST ------------
    public void rejectFollow(String me, String follower) {

        followRepository.deleteByFollowerUsernameAndFolloweeUsername(
                follower,
                me
        );
    }

    // ------------ UNFOLLOW (mutation allowed only after approval) ------------
    public void unfollow(String follower, String followee) {

        followRepository.deleteByFollowerUsernameAndFolloweeUsername(
                follower,
                followee
        );
    }

    // ------------ CHECK FOLLOWING STATUS ------------
    public boolean isFollowing(String follower, String followee) {

        return followRepository.existsByFollowerUsernameAndFolloweeUsernameAndStatus(
                follower,
                followee,
                Follow.Status.APPROVED
        );
    }

    // ------------ WHO I FOLLOW ------------
    public List<String> getFollowingUsernames(String follower) {
        return followRepository
                .findByFollowerUsernameAndStatus(
                        follower,
                        Follow.Status.APPROVED
                )
                .stream()
                .map(Follow::getFolloweeUsername)
                .toList();
    }

    // ------------ WHO FOLLOWS ME ------------
    public List<String> getFollowerUsernames(String followee) {
        return followRepository
                .findByFolloweeUsernameAndStatus(
                        followee,
                        Follow.Status.APPROVED
                )
                .stream()
                .map(Follow::getFollowerUsername)
                .toList();
    }

    // ------------ VIEW PENDING REQUESTS ------------
    public List<String> getPendingRequests(String me) {

        return followRepository
                .findByFolloweeUsernameAndStatus(
                        me,
                        Follow.Status.PENDING
                )
                .stream()
                .map(Follow::getFollowerUsername)
                .toList();
    }
}
