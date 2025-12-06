package com.DailyBook.repository;

import com.DailyBook.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientUsernameOrderByCreatedAtDesc(
            String recipientUsername,
            Pageable pageable
    );

    long countByRecipientUsernameAndReadIsFalse(String recipientUsername);
}
