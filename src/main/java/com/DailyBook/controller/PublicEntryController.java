// com/DailyBook/controller/PublicEntryController.java
package com.DailyBook.controller;

import com.DailyBook.dto.EntryResponse;
import com.DailyBook.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicEntryController {

    private final EntryService entryService;

    // Explore: latest public posts (optional tag filter)
    @GetMapping("/entries")
    public Page<EntryResponse> listPublic(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String tag
    ) {
        return entryService.listPublic(page, size, tag);
    }

    // Search across public posts (title/content/tags)
    @GetMapping("/entries/search")
    public Page<EntryResponse> searchPublic(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return entryService.searchPublic(q, page, size);
    }
}
