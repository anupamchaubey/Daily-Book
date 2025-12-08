package com.DailyBook.repository;

import com.DailyBook.model.Entry;
import com.DailyBook.model.Entry.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EntryRepository extends MongoRepository<Entry, String> {

    // ===== BASIC FETCH =====

    List<Entry> findByUserId(String userId);

    Page<Entry> findByUserIdAndVisibilityOrderByCreatedAtDesc(
            String userId,
            Visibility visibility,
            Pageable pageable
    );
    Page<Entry> findByUserIdAndVisibilityInOrderByCreatedAtDesc(
            String userId,
            List<Entry.Visibility> visibilities,
            Pageable pageable
    );


    Page<Entry> findByVisibilityOrderByCreatedAtDesc(
            Visibility visibility,
            Pageable pageable
    );

    Page<Entry> findByVisibilityAndTagsContainingIgnoreCase(
            Visibility visibility,
            String tag,
            Pageable pageable
    );

    Page<Entry> findByUserIdInAndVisibilityIn(
            List<String> usernames,
            List<Visibility> visibilities,
            Pageable pageable
    );


    // ===== SEARCH =====

    // 🔍 Search PUBLIC posts (any user)
    @Query("""
    {
      $and: [
        { visibility: ?0 },
        { $or: [
            { title:   { $regex: ?1, $options: 'i' } },
            { content: { $regex: ?1, $options: 'i' } },
            { tags:    { $regex: ?1, $options: 'i' } }
        ]}
      ]
    }
    """)
    Page<Entry> searchPublic(
            Visibility visibility,
            String query,
            Pageable pageable
    );


    // 🔍 Search MY posts only (with allowed visibilities)
    @Query("""
    {
      $and: [
        { userId: ?0 },
        { visibility : { $in : ?1 } },
        { $or: [
            { title:   { $regex: ?2, $options: 'i' } },
            { content: { $regex: ?2, $options: 'i' } },
            { tags:    { $regex: ?2, $options: 'i' } }
        ]}
      ]
    }
    """)
    Page<Entry> searchByUserAndVisibilities(
            String userId,
            List<Visibility> visibilities,
            String query,
            Pageable pageable
    );


    // 🔍 Search FOLLOWED users posts (followers-only posts)
    @Query("""
    {
      $and: [
        { userId : { $in : ?0 } },
        { visibility : { $in : ?1 } },
        { $or: [
            { title:   { $regex: ?2, $options: 'i' } },
            { content: { $regex: ?2, $options: 'i' } },
            { tags:    { $regex: ?2, $options: 'i' } }
        ]}
      ]
    }
    """)
    Page<Entry> searchByUsersAndVisibilities(
            List<String> userIds,
            List<Visibility> visibilities,
            String query,
            Pageable pageable
    );

}
