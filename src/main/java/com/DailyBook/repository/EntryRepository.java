package com.DailyBook.repository;

import com.DailyBook.model.Entry;
import com.DailyBook.model.Entry.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EntryRepository extends MongoRepository<Entry, String> {

    // Fetch entries authored by a specific user
    Page<Entry> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Fetch entries authored by a user with specific visibilities
    Page<Entry> findByUserIdAndVisibilityInOrderByCreatedAtDesc(
            String userId,
            List<Visibility> visibilities,
            Pageable pageable
    );

    // Fetch all public entries
    Page<Entry> findByVisibilityOrderByCreatedAtDesc(Visibility visibility, Pageable pageable);

    // Fetch entries by multiple users with specific visibilities (e.g. for feed)
    Page<Entry> findByUserIdInAndVisibilityInOrderByCreatedAtDesc(
            List<String> userIds,
            List<Visibility> visibilities,
            Pageable pageable
    );

    // Search public entries by keyword in title, content, or tags
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
}
