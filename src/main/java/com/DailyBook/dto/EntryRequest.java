package com.DailyBook.dto;

import com.DailyBook.model.Entry;
import lombok.Data;

import java.util.List;

@Data
public class EntryRequest {
    private String title;
    private String content;
    private List<String> tags;
    private Entry.Visibility visibility;
}
