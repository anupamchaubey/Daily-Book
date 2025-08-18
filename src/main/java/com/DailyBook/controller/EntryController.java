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

    // Create Entry
    @PostMapping
    public EntryResponse createEntry(@Valid @RequestBody EntryRequest request, Authentication authentication) {
        return entryService.createEntry(request, authentication.getName());
    }

    // Get all user entries
    @GetMapping
    public List<EntryResponse> getUserEntries(Authentication authentication) {
        return entryService.getUserEntries(authentication.getName());
    }

    // Get entry by ID
    @GetMapping("/{id}")
    public EntryResponse getEntry(@PathVariable String id, Authentication authentication) {
        return entryService.getEntryByIdForUser(id, authentication.getName());
    }

    // Update entry
    @PutMapping("/{id}")
    public EntryResponse updateEntry(@PathVariable String id,
                                     @Valid @RequestBody EntryRequest request,
                                     Authentication authentication) {
        return entryService.updateEntry(id, request, authentication.getName());
    }

    // Delete entry
    @DeleteMapping("/{id}")
    public String deleteEntry(@PathVariable String id, Authentication authentication) {
        entryService.deleteEntry(id, authentication.getName());
        return "Entry deleted successfully";
    }

    // ==============================
    // 🔹 PUBLIC (no login required)
    // ==============================

    // List all public entries (optionally filtered by tag)
    @GetMapping("/public")
    public Page<EntryResponse> listPublic(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String tag) {
        return entryService.listPublic(page, size, tag);
    }

    // List public entries by username
    @GetMapping("/public/user/{username}")
    public Page<EntryResponse> listPublicByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return entryService.listPublicByUsername(username, page, size);
    }

    // Search public entries
    @GetMapping("/public/search")
    public Page<EntryResponse> searchPublic(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return entryService.searchPublic(q, page, size);
    }
}
