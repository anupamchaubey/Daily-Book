package com.DailyBook.repository;

import com.DailyBook.model.Entry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EntryRepository extends MongoRepository<Entry, String> {
    List<Entry> findByUserId(String userId);
    List<Entry> findByUserIdAndTagsContaining(String userId, String tag);
    List<Entry> findByVisibility(Entry.Visibility visibility);
}
