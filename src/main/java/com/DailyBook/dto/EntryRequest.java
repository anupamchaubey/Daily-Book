package com.DailyBook.dto;

import com.DailyBook.model.Entry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EntryRequest {

    @NotBlank(message = "Title is required ")
    @Size(min=3,max = 100,message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message="Content cannot be empty")
    @Size(min=10,message = "Content must be atleast 10 characters")
    private String content;


    private List<String> tags;

    private Entry.Visibility visibility;
}
