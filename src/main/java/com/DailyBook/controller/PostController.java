package com.DailyBook.controller;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final EntryService entryService;

    // Create a new post (authenticated)
    @PostMapping
    public ResponseEntity<EntryResponse> createPost(
            @Valid @RequestBody EntryRequest request,
            Authentication authentication
    ) {
        EntryResponse response = entryService.createEntry(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get a specific post by ID (public if PUBLIC; requires auth/follow for PRIVATE/FOLLOWERS_ONLY)
    @GetMapping("/{id}")
    public ResponseEntity<EntryResponse> getPost(
            @PathVariable String id,
            Authentication authentication
    ) {
        String viewer = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(entryService.getEntryById(id, viewer));
    }

    // Update own post (author only)
    @PutMapping("/{id}")
    public ResponseEntity<EntryResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody EntryRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(entryService.updateEntry(id, request, authentication.getName()));
    }

    // Delete own post (author only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable String id,
            Authentication authentication
    ) {
        entryService.deleteEntry(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    // List logged-in user's own posts
    @GetMapping("/me")
    public ResponseEntity<Page<EntryResponse>> getMyPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(entryService.getMyEntries(authentication.getName(), page, size));
    }

    // Explore / public posts
    @GetMapping
    public ResponseEntity<Page<EntryResponse>> listPublicPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(entryService.listPublicEntries(page, size));
    }

    // User's post list (visibility-aware based on current viewer)
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<EntryResponse>> getPostsByAuthor(
            @PathVariable String username,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String viewer = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(entryService.getEntriesByAuthor(username, viewer, page, size));
    }

    // User timeline feed (own posts + posts of followed authors)
    @GetMapping("/feed")
    public ResponseEntity<Page<EntryResponse>> getFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(entryService.getFeed(authentication.getName(), page, size));
    }

    // Simple keyword search across public posts
    @GetMapping("/search")
    public ResponseEntity<Page<EntryResponse>> searchPublicPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }
        return ResponseEntity.ok(entryService.searchPublicEntries(q.trim(), page, size));
    }
}
