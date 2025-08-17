package com.DailyBook.controller;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.model.Entry;
import com.DailyBook.service.EntryService;
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
    public EntryResponse createEntry(@RequestBody EntryRequest request, Authentication authentication) {
        return entryService.createEntry(request, authentication.getName());
    }

    // ✅ Get all user entries
    @GetMapping
    public List<EntryResponse> getUserEntries(Authentication authentication) {
        return entryService.getUserEntries(authentication.getName());
    }

    // ✅ Get entry by ID
    @GetMapping("/{id}")
    public EntryResponse getEntry(@PathVariable String id) {
        return entryService.getEntryById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
    }

    // ✅ Update entry
    @PutMapping("/{id}")
    public EntryResponse updateEntry(@PathVariable String id,
                                     @RequestBody EntryRequest request,
                                     Authentication authentication) {

        Entry existing = entryService.entryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (!existing.getUserId().equals(authentication.getName())) {
            throw new RuntimeException("You are not allowed to edit this entry");
        }

        return entryService.updateEntry(existing, request);
    }

    // ✅ Delete entry
    @DeleteMapping("/{id}")
    public String deleteEntry(@PathVariable String id, Authentication authentication) {
        Entry existing = entryService.entryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (!existing.getUserId().equals(authentication.getName())) {
            throw new RuntimeException("You are not allowed to delete this entry");
        }

        entryService.deleteEntry(id);
        return "Entry deleted successfully";
    }
}
