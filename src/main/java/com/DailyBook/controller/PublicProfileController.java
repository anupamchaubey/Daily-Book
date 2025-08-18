// com/DailyBook/controller/PublicProfileController.java
package com.DailyBook.controller;

import com.DailyBook.dto.EntryResponse;
import com.DailyBook.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class PublicProfileController {

    private final EntryService entryService;

    // Public author page: username -> their public posts
    @GetMapping("/{username}/entries")
    public Page<EntryResponse> listByAuthor(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return entryService.listPublicByUsername(username, page, size);
    }
}
