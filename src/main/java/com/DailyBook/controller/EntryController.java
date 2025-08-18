package com.DailyBook.controller;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.model.Entry;
import com.DailyBook.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    // ✅ Create Entry
    @PostMapping
    public EntryResponse createEntry(@Valid @RequestBody EntryRequest request, Authentication authentication) {
        return entryService.createEntry(request, authentication.getName());
    }

    // ✅ Get all user entries
    @GetMapping
    public List<EntryResponse> getUserEntries(Authentication authentication) {
        return entryService.getUserEntries(authentication.getName());
    }

    // ✅ Get entry by ID
    @GetMapping("/{id}")
    public EntryResponse getEntry(@PathVariable String id, Authentication authentication) {
        return entryService.getEntryByIdForUser(id, authentication.getName());
    }

    // ✅ Update entry
    @PutMapping("/{id}")
    public EntryResponse updateEntry(@PathVariable String id,
                                     @Valid @RequestBody EntryRequest request,
                                     Authentication authentication) {
        return entryService.updateEntry(id, request, authentication.getName());
    }

    // ✅ Delete entry
    @DeleteMapping("/{id}")
    public String deleteEntry(@PathVariable String id, Authentication authentication) {
        entryService.deleteEntry(id, authentication.getName());
        return "Entry deleted successfully";
    }
}
