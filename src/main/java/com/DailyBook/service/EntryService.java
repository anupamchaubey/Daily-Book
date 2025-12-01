package com.DailyBook.service;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.exception.EntryNotFoundException;
import com.DailyBook.model.Entry;
import com.DailyBook.model.UserProfile;
import com.DailyBook.repository.EntryRepository;
import com.DailyBook.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final UserProfileRepository userProfileRepository;

    // ✅ Create Entry
    public EntryResponse createEntry(EntryRequest request, String userId /* actually username */) {
        Entry entry = Entry.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .visibility(request.getVisibility() != null
                        ? request.getVisibility()
                        : Entry.Visibility.PRIVATE)
                .build();
        return toResponse(entryRepository.save(entry));
    }

    // ✅ Get all entries of logged-in user
    public List<EntryResponse> getUserEntries(String userId) {
        return entryRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Get entry by ID (ownership check)
    public EntryResponse getEntryByIdForUser(String entryId, String userId) {
        Entry entry = getEntryOrThrow(entryId);
        if (!entry.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        return toResponse(entry);
    }

    // ✅ Update entry
    public EntryResponse updateEntry(String entryId, EntryRequest request, String userId) {
        Entry existing = getEntryOrThrow(entryId);
        if (!existing.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setTags(request.getTags());
        if (request.getVisibility() != null) {
            existing.setVisibility(request.getVisibility());
        }
        return toResponse(entryRepository.save(existing));
    }

    // ✅ Delete entry
    public void deleteEntry(String entryId, String userId) {
        Entry entry = getEntryOrThrow(entryId);
        if (!entry.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        entryRepository.delete(entry);
    }

    // ✅ Public: list entries
    public Page<EntryResponse> listPublic(Integer page, Integer size, String tag) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Entry> entries = (tag == null || tag.isBlank())
                ? entryRepository.findByVisibilityOrderByCreatedAtDesc(Entry.Visibility.PUBLIC, pageable)
                : entryRepository.findByVisibilityAndTagsContainingIgnoreCase(Entry.Visibility.PUBLIC, tag, pageable);
        return entries.map(this::toResponse);
    }

    // ✅ Public: list entries by username (author page)
    public Page<EntryResponse> listPublicByUsername(String username, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Entry> entries = entryRepository
                .findByUserIdAndVisibilityOrderByCreatedAtDesc(username, Entry.Visibility.PUBLIC, pageable);
        return entries.map(this::toResponse);
    }

    // ✅ Public: search entries
    public Page<EntryResponse> searchPublic(String q, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return entryRepository.searchPublic(Entry.Visibility.PUBLIC, q, pageable)
                .map(this::toResponse);
    }

    // 🔹 Helper
    private Entry getEntryOrThrow(String entryId) {
        return entryRepository.findById(entryId)
                .orElseThrow(() -> new EntryNotFoundException("Entry not found with id: " + entryId));
    }

    // 🔹 Mapper
    private EntryResponse toResponse(Entry entry) {
        UserProfile profile = userProfileRepository.findById(entry.getUserId()).orElse(null);
        return EntryResponse.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .tags(entry.getTags())
                .visibility(entry.getVisibility())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .authorId(entry.getUserId())
                .authorUsername(profile != null ? profile.getUsername() : "Unknown")
                .authorProfilePicture(profile != null ? profile.getProfilePicture() : null)
                .build();
    }

    // ✅ Simplified feed: public entries only
    public Page<EntryResponse> listVisibleEntries(String viewerId, Integer page, Integer size, String tag) {
        return listPublic(page, size, tag);
    }
}
