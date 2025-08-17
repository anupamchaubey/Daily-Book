package com.DailyBook.dto;

import com.DailyBook.model.Entry;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class EntryResponse {
    private String id;
    private String title;
    private String content;
    private List<String> tags;
    private Entry.Visibility visibility;
    private Instant createdAt;
    private Instant updatedAt;
}
