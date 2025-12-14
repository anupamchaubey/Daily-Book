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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.DailyBook.model.Entry.Visibility.FOLLOWERS_ONLY;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final FollowService followService;
    private final EntryRepository entryRepository;
    private final UserProfileRepository userProfileRepository;

    // =========================
    //  Visibility-aware Get by ID
    // =========================
    public EntryResponse getEntryVisibleToViewer(String entryId, String viewerUsername) {
        Entry entry = getEntryOrThrow(entryId);

        Entry.Visibility visibility = entry.getVisibility();
        String authorUsername = entry.getUserId(); // you store username here

        // 1) PUBLIC → always allowed
        if (visibility == Entry.Visibility.PUBLIC) {
            return toResponse(entry);
        }

        // 2) Not logged in → cannot see PRIVATE or FOLLOWERS_ONLY
        if (viewerUsername == null) {
            throw new EntryNotFoundException("Entry not visible");
        }

        // 3) Owner can always see
        if (authorUsername.equals(viewerUsername)) {
            return toResponse(entry);
        }

        // 4) Followers-only: allowed only if viewer follows author
        if (visibility == FOLLOWERS_ONLY &&
                followService.isFollowing(viewerUsername, authorUsername)) {
            return toResponse(entry);
        }

        // 5) Otherwise → behave like "not found"
        throw new EntryNotFoundException("Entry not visible");
    }

    // =========================
    //  Visibility-aware Search
    // =========================
    public Page<EntryResponse> searchVisibleEntries(
            String viewerUsername,
            String query,
            Integer page,
            Integer size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        // 🔓 Logged-out users: only PUBLIC posts, any author
        if (viewerUsername == null) {
            return entryRepository
                    .searchPublic(Entry.Visibility.PUBLIC, query, pageable)
                    .map(this::toResponse);
        }

        // 👥 Logged-in users
        List<String> following = followService.getFollowingUsernames(viewerUsername);

        // 1️⃣ My posts: all visibilities
        Page<Entry> mine =
                entryRepository.searchByUserAndVisibilities(
                        viewerUsername,
                        List.of(
                                Entry.Visibility.PUBLIC,
                                Entry.Visibility.PRIVATE,
                                FOLLOWERS_ONLY
                        ),
                        query,
                        pageable
                );

        // 2️⃣ Public posts from everyone
        Page<Entry> publicPosts =
                entryRepository.searchPublic(
                        Entry.Visibility.PUBLIC,
                        query,
                        pageable
                );

        // 3️⃣ Followers-only posts from people I follow
        Page<Entry> followerPosts =
                following.isEmpty()
                        ? Page.empty(pageable)
                        : entryRepository.searchByUsersAndVisibilities(
                        following,
                        List.of(FOLLOWERS_ONLY),
                        query,
                        pageable
                );

        // 🔄 Merge, dedupe, sort newest-first
        List<Entry> merged = new ArrayList<>();
        merged.addAll(mine.getContent());
        merged.addAll(publicPosts.getContent());
        merged.addAll(followerPosts.getContent());

        merged = merged.stream()
                .distinct()
                .sorted(
                        Comparator.comparing(Entry::getCreatedAt)
                                .reversed()
                )
                .collect(Collectors.toList());

        // 📄 Manual paging
        int start = Math.min(page * size, merged.size());
        int end = Math.min(start + size, merged.size());

        List<EntryResponse> pageContent =
                (start >= end)
                        ? List.of()
                        : merged.subList(start, end)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new PageImpl<>(pageContent, pageable, merged.size());
    }

    // =========================
    //  CRUD
    // =========================

    // userId here is actually the username (from authentication.getName())
    public EntryResponse createEntry(EntryRequest request, String userId /* username */) {
        Entry entry = Entry.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .visibility(request.getVisibility() != null
                        ? request.getVisibility()
                        : Entry.Visibility.PRIVATE)
                .imageUrls(request.getImageUrls())
                .build();

        return toResponse(entryRepository.save(entry));
    }

    public Page<EntryResponse> listVisibleByUsername(
            String viewerUsername,
            String authorUsername,
            Integer page,
            Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // 1) Viewer is not logged in → only PUBLIC
        if (viewerUsername == null) {
            return listPublicByUsername(authorUsername, page, size);
        }

        // 2) Viewer is the author → all visibilities
        if (viewerUsername.equals(authorUsername)) {
            // PUBLIC + PRIVATE + FOLLOWERS_ONLY
            List<Entry.Visibility> visibilities = List.of(
                    Entry.Visibility.PUBLIC,
                    Entry.Visibility.PRIVATE,
                    FOLLOWERS_ONLY
            );
            Page<Entry> entries = entryRepository
                    .findByUserIdAndVisibilityInOrderByCreatedAtDesc(
                            authorUsername,
                            visibilities,
                            pageable
                    );
            return entries.map(this::toResponse);
        }

        // 3) Viewer follows the author → PUBLIC + FOLLOWERS_ONLY
        boolean isFollower = followService.isFollowing(viewerUsername, authorUsername);
        if (isFollower) {
            List<Entry.Visibility> visibilities = List.of(
                    Entry.Visibility.PUBLIC,
                    FOLLOWERS_ONLY
            );
            Page<Entry> entries = entryRepository
                    .findByUserIdAndVisibilityInOrderByCreatedAtDesc(
                            authorUsername,
                            visibilities,
                            pageable
                    );
            return entries.map(this::toResponse);
        }

        // 4) Viewer does not follow → only PUBLIC
        return listPublicByUsername(authorUsername, page, size);
    }

    public List<EntryResponse> getUserEntries(String userId) {
        return entryRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EntryResponse getEntryByIdForUser(String entryId, String userId) {
        Entry entry = getEntryOrThrow(entryId);
        if (!entry.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        return toResponse(entry);
    }

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

        if (request.getImageUrls() != null) {
            existing.setImageUrls(request.getImageUrls());
        }

        return toResponse(entryRepository.save(existing));
    }

    public void deleteEntry(String entryId, String userId) {
        Entry entry = getEntryOrThrow(entryId);
        if (!entry.getUserId().equals(userId)) {
            throw new EntryNotFoundException("Entry not found for this user");
        }
        entryRepository.delete(entry);
    }

    // =========================
    //  Public listing
    // =========================

    public Page<EntryResponse> listPublic(Integer page, Integer size, String tag) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Entry> entries = (tag == null || tag.isBlank())
                ? entryRepository.findByVisibilityOrderByCreatedAtDesc(Entry.Visibility.PUBLIC, pageable)
                : entryRepository.findByVisibilityAndTagsContainingIgnoreCase(Entry.Visibility.PUBLIC, tag, pageable);

        return entries.map(this::toResponse);
    }

    public Page<EntryResponse> listPublicByUsername(String username, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Entry> entries = entryRepository
                .findByUserIdAndVisibilityOrderByCreatedAtDesc(username, Entry.Visibility.PUBLIC, pageable);

        return entries.map(this::toResponse);
    }

    public Page<EntryResponse> searchPublic(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Entry> result;
        if (q.length() >= 3) {
            result = entryRepository.searchPublicText(
                    Entry.Visibility.PUBLIC, q, pageable
            );
        } else {
            result = entryRepository.searchPublic(
                    Entry.Visibility.PUBLIC, q, pageable
            );
        }

        return result.map(this::toResponse);
    }



    // =========================
    //  Feed (visibility-aware)
    // =========================
    public Page<EntryResponse> listVisibleEntries(String viewerUsername,
                                                  Integer page,
                                                  Integer size,
                                                  String tag) {

        Pageable pageable = PageRequest.of(page, size);

        // 🔓 logged out -> PUBLIC only
        if (viewerUsername == null) {
            return listPublic(page, size, tag);
        }

        // 👥 logged in
        List<String> following = followService.getFollowingUsernames(viewerUsername);

        // build allowed visibilities for followed users
        List<Entry.Visibility> allowed = List.of(
                Entry.Visibility.PUBLIC,
                FOLLOWERS_ONLY
        );

        // followers' posts (PUBLIC + FOLLOWERS_ONLY)
        Page<Entry> followerPosts = following.isEmpty()
                ? Page.empty(pageable)
                : entryRepository.findByUserIdInAndVisibilityIn(
                following,
                allowed,
                pageable
        );

        // own posts (all visibilities)
        List<Entry> myEntries = entryRepository.findByUserId(viewerUsername);

        // combine
        List<Entry> merged = new ArrayList<>();
        merged.addAll(myEntries);
        merged.addAll(followerPosts.getContent());

        // remove duplicates & sort newest-first
        merged = merged.stream()
                .distinct()
                .sorted(
                        Comparator.comparing(Entry::getCreatedAt)
                                .reversed()
                )
                .collect(Collectors.toList());

        // manual pagination
        int start = Math.min(page * size, merged.size());
        int end = Math.min(start + size, merged.size());

        List<Entry> pageContent = merged.subList(start, end);

        return new PageImpl<>(
                pageContent.stream().map(this::toResponse).toList(),
                pageable,
                merged.size()
        );
    }

    // =========================
    //  Helpers
    // =========================

    private Entry getEntryOrThrow(String entryId) {
        return entryRepository.findById(entryId)
                .orElseThrow(() -> new EntryNotFoundException("Entry not found with id: " + entryId));
    }

    private EntryResponse toResponse(Entry entry) {
        UserProfile profile = userProfileRepository.findById(entry.getUserId()).orElse(null);

        // ensure non-null lists to avoid nulls in frontend
        List<String> imageUrls = entry.getImageUrls() != null ? entry.getImageUrls() : List.of();
        List<String> tags = entry.getTags() != null ? entry.getTags() : List.of();

        return EntryResponse.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .tags(tags)
                .visibility(entry.getVisibility())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .imageUrls(imageUrls)
                .authorId(entry.getUserId())
                .authorUsername(profile != null ? profile.getUsername() : "Unknown")
                .authorProfilePicture(profile != null ? profile.getProfilePicture() : null)
                .build();
    }

}
