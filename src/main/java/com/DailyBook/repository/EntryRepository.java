// com/DailyBook/repository/EntryRepository.java
package com.DailyBook.repository;

import com.DailyBook.model.Entry;
import com.DailyBook.model.Entry.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EntryRepository extends MongoRepository<Entry, String> {

    List<Entry> findByUserId(String userId);


    // ✅ Get all public entries (latest first)
    Page<Entry> findByVisibilityOrderByCreatedAtDesc(Visibility visibility, Pageable pageable);

    // ✅ Filter by tag (case-insensitive)
    Page<Entry> findByVisibilityAndTagsContainingIgnoreCase(Visibility visibility, String tag, Pageable pageable);

    // ✅ User's public posts (profile page)
    Page<Entry> findByUserIdAndVisibilityOrderByCreatedAtDesc(String userId, Visibility visibility, Pageable pageable);

    // ✅ Full-text style search in title, content, and tags
    @Query(value = """
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
    Page<Entry> searchPublic(Visibility visibility, String query, Pageable pageable);
}
