package com.DailyBook.service;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.exception.EntryNotFoundException;
import com.DailyBook.model.Entry;
import com.DailyBook.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.DailyBook.model.Entry.Visibility.FOLLOWERS_ONLY;
import static com.DailyBook.model.Entry.Visibility.PRIVATE;
import static com.DailyBook.model.Entry.Visibility.PUBLIC;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final FollowService followService;
    private final EntryRepository entryRepository;

    // ==========================================
    // 1. CREATE ENTRY
    // ==========================================
    public EntryResponse createEntry(EntryRequest request, String authorUsername) {
        Entry entry = Entry.builder()
                .userId(authorUsername)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags() != null ? request.getTags() : List.of())
                .visibility(request.getVisibility() != null ? request.getVisibility() : PRIVATE)
                .build();

        return toResponse(entryRepository.save(entry));
    }

    // ==========================================
    // 2. GET ENTRY BY ID (Visibility-aware)
    // ==========================================
    public EntryResponse getEntryById(String entryId, String viewerUsername) {
        Entry entry = getEntryOrThrow(entryId);
        String authorUsername = entry.getUserId();

        // Rule 1: PUBLIC is visible to everyone (authenticated or anonymous)
        if (entry.getVisibility() == PUBLIC) {
            return toResponse(entry);
        }

        // Anonymous viewers cannot see PRIVATE or FOLLOWERS_ONLY posts
        if (viewerUsername == null) {
            throw new EntryNotFoundException("Entry not found with id: " + entryId);
        }

        // Rule 2: The author can always see their own posts
        if (authorUsername.equals(viewerUsername)) {
            return toResponse(entry);
        }

        // Rule 3: FOLLOWERS_ONLY posts can be viewed by approved followers
        if (entry.getVisibility() == FOLLOWERS_ONLY &&
                followService.isFollowing(viewerUsername, authorUsername)) {
            return toResponse(entry);
        }

        // Rule 4: Otherwise, return 404 (do not leak existence of private/unauthorized post)
        throw new EntryNotFoundException("Entry not found with id: " + entryId);
    }

    // ==========================================
    // 3. UPDATE ENTRY (Author only)
    // ==========================================
    public EntryResponse updateEntry(String entryId, EntryRequest request, String currentUsername) {
        Entry entry = getEntryOrThrow(entryId);

        if (!entry.getUserId().equals(currentUsername)) {
            throw new AccessDeniedException("You are not authorized to update this entry");
        }

        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        if (request.getTags() != null) {
            entry.setTags(request.getTags());
        }
        if (request.getVisibility() != null) {
            entry.setVisibility(request.getVisibility());
        }

        return toResponse(entryRepository.save(entry));
    }

    // ==========================================
    // 4. DELETE ENTRY (Author only)
    // ==========================================
    public void deleteEntry(String entryId, String currentUsername) {
        Entry entry = getEntryOrThrow(entryId);

        if (!entry.getUserId().equals(currentUsername)) {
            throw new AccessDeniedException("You are not authorized to delete this entry");
        }

        entryRepository.delete(entry);
    }

    // ==========================================
    // 5. LIST ENTRIES BY AUTHOR (Visibility-aware)
    // ==========================================
    public Page<EntryResponse> getEntriesByAuthor(String authorUsername, String viewerUsername, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Case 1: Viewing own posts -> all posts (PUBLIC, PRIVATE, FOLLOWERS_ONLY)
        if (viewerUsername != null && viewerUsername.equals(authorUsername)) {
            return entryRepository
                    .findByUserIdOrderByCreatedAtDesc(authorUsername, pageable)
                    .map(this::toResponse);
        }

        // Case 2: Viewer follows the author -> PUBLIC and FOLLOWERS_ONLY
        if (viewerUsername != null && followService.isFollowing(viewerUsername, authorUsername)) {
            List<Entry.Visibility> visibilities = List.of(PUBLIC, FOLLOWERS_ONLY);
            return entryRepository
                    .findByUserIdAndVisibilityInOrderByCreatedAtDesc(authorUsername, visibilities, pageable)
                    .map(this::toResponse);
        }

        // Case 3: Anonymous viewer or non-follower -> PUBLIC only
        return entryRepository
                .findByUserIdAndVisibilityInOrderByCreatedAtDesc(authorUsername, List.of(PUBLIC), pageable)
                .map(this::toResponse);
    }

    // ==========================================
    // 6. LIST LOGGED-IN USER'S OWN ENTRIES
    // ==========================================
    public Page<EntryResponse> getMyEntries(String currentUsername, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entryRepository
                .findByUserIdOrderByCreatedAtDesc(currentUsername, pageable)
                .map(this::toResponse);
    }

    // ==========================================
    // 7. LIST PUBLIC ENTRIES (Explore)
    // ==========================================
    public Page<EntryResponse> listPublicEntries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entryRepository
                .findByVisibilityOrderByCreatedAtDesc(PUBLIC, pageable)
                .map(this::toResponse);
    }

    // ==========================================
    // 8. TIMELINE FEED (Visibility-aware)
    // ==========================================
    public Page<EntryResponse> getFeed(String currentUsername, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<String> following = followService.getFollowingUsernames(currentUsername);
        List<String> targetUsers = new ArrayList<>(following);
        targetUsers.add(currentUsername);

        // Include own posts and posts of followed users that are PUBLIC or FOLLOWERS_ONLY
        List<Entry.Visibility> visibilities = List.of(PUBLIC, FOLLOWERS_ONLY);

        return entryRepository
                .findByUserIdInAndVisibilityInOrderByCreatedAtDesc(targetUsers, visibilities, pageable)
                .map(this::toResponse);
    }

    // ==========================================
    // 9. SEARCH PUBLIC ENTRIES
    // ==========================================
    public Page<EntryResponse> searchPublicEntries(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entryRepository
                .searchPublic(PUBLIC, query, pageable)
                .map(this::toResponse);
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private Entry getEntryOrThrow(String entryId) {
        return entryRepository.findById(entryId)
                .orElseThrow(() -> new EntryNotFoundException("Entry not found with id: " + entryId));
    }

    private EntryResponse toResponse(Entry entry) {
        return EntryResponse.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .tags(entry.getTags() != null ? entry.getTags() : List.of())
                .visibility(entry.getVisibility())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .authorId(entry.getUserId())
                .build();
    }
}
