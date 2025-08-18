package com.DailyBook.service;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.exception.EntryNotFoundException;
import com.DailyBook.model.Entry;
import com.DailyBook.model.UserProfile;
import com.DailyBook.repository.EntryRepository;
import com.DailyBook.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final UserProfileRepository userProfileRepository;

    // ✅ Create Entry
    public EntryResponse createEntry(EntryRequest request, String userId) {
        Entry entry = Entry.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .visibility(request.getVisibility())
                .build();

        Entry saved = entryRepository.save(entry);
        return toResponse(saved);
    }

    // ✅ Get all entries of logged-in user
    public List<EntryResponse> getUserEntries(String userId) {
        return entryRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Get entry by ID for a user (ownership check)
    public EntryResponse getEntryByIdForUser(String entryId, String userId) {
        Entry entry = getEntryOrThrow(entryId);
        if (!entry.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        return toResponse(entry);
    }

    // ✅ Update entry (only if belongs to user)
    public EntryResponse updateEntry(String entryId, EntryRequest request, String userId) {
        Entry existing = getEntryOrThrow(entryId);
        if (!existing.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }

        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setTags(request.getTags());
        existing.setVisibility(request.getVisibility());

        Entry updated = entryRepository.save(existing);
        return toResponse(updated);
    }

    // ✅ Delete entry (only if belongs to user)
    public void deleteEntry(String entryId, String userId) {
        Entry entry = getEntryOrThrow(entryId);
        if (!entry.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        entryRepository.delete(entry);
    }

    // 🔹 Helper: fetch or throw exception
    private Entry getEntryOrThrow(String entryId) {
        return entryRepository.findById(entryId)
                .orElseThrow(() -> new EntryNotFoundException("Entry not found with id: " + entryId));
    }

    // 🔹 Mapper: Entity → Response DTO (with author info)
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
}
