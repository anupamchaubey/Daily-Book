package com.DailyBook.controller;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    // ==============================
    // 🔹 PRIVATE (requires login)
    // ==============================

    @PostMapping
    public EntryResponse createEntry(@Valid @RequestBody EntryRequest request, Authentication authentication) {
        return entryService.createEntry(request, authentication.getName());
    }

    @GetMapping
    public List<EntryResponse> getUserEntries(Authentication authentication) {
        return entryService.getUserEntries(authentication.getName());
    }

    @GetMapping("/{id}")
    public EntryResponse getEntry(@PathVariable String id, Authentication authentication) {
        String viewer = (authentication != null) ? authentication.getName() : null;
        return entryService.getEntryVisibleToViewer(id, viewer);
    }



    @PutMapping("/{id}")
    public EntryResponse updateEntry(@PathVariable String id,
                                     @Valid @RequestBody EntryRequest request,
                                     Authentication authentication) {
        return entryService.updateEntry(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public String deleteEntry(@PathVariable String id, Authentication authentication) {
        entryService.deleteEntry(id, authentication.getName());
        return "Entry deleted successfully";
    }

    // ==============================
    // 🔹 PUBLIC (no login required)
    // ==============================

    @GetMapping("/public")
    public Page<EntryResponse> listPublic(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String tag) {
        return entryService.listPublic(page, size, tag);
    }

    @GetMapping("/public/user/{username}")
    public Page<EntryResponse> listVisibleByUser(
            Authentication authentication,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        String viewer = (authentication != null) ? authentication.getName() : null;
        return entryService.listVisibleByUsername(viewer, username, page, size);
    }


    @GetMapping("/public/search")
    public Page<EntryResponse> search(
            Authentication authentication,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        String viewer = authentication != null ? authentication.getName() : null;
        return entryService.searchVisibleEntries(viewer, q, page, size);
    }


    // 🔹 Feed now just returns public entries
    @GetMapping("/feed")
    public Page<EntryResponse> getFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String tag) {

        String viewer = authentication != null ? authentication.getName() : null;
        return entryService.listVisibleEntries(viewer, page, size, tag);
    }

}
