package com.DailyBook.service;

import com.DailyBook.dto.EntryRequest;
import com.DailyBook.dto.EntryResponse;
import com.DailyBook.model.Entry;
import com.DailyBook.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    public final EntryRepository entryRepository;

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

    public List<EntryResponse> getUserEntries(String userId) {
        return entryRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<EntryResponse> getEntryById(String id) {
        return entryRepository.findById(id).map(this::toResponse);
    }

    public EntryResponse updateEntry(Entry existing, EntryRequest request) {
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setTags(request.getTags());
        existing.setVisibility(request.getVisibility());
        Entry updated = entryRepository.save(existing);
        return toResponse(updated);
    }

    public void deleteEntry(String entryId) {
        entryRepository.deleteById(entryId);
    }

    // ✅ Mapper: Entity → Response DTO
    private EntryResponse toResponse(Entry entry) {
        return EntryResponse.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .tags(entry.getTags())
                .visibility(entry.getVisibility())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
