package com.iksanov.citytour.attraction.controller;

import com.iksanov.citytour.attraction.dto.AttractionRequest;
import com.iksanov.citytour.attraction.dto.AttractionResponse;
import com.iksanov.citytour.attraction.entity.AttractionCategory;
import com.iksanov.citytour.attraction.service.AttractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @PostMapping
    public ResponseEntity<AttractionResponse> create(@Valid @RequestBody AttractionRequest request) {
        AttractionResponse response = attractionService.create(request);
        URI location = URI.create("/api/attractions/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttractionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                attractionService.getById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<AttractionResponse>> getAll(
            @RequestParam(required = false) AttractionCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                attractionService.getAll(
                        category,
                        search,
                        pageable
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttractionResponse> update(@PathVariable Long id, @Valid @RequestBody AttractionRequest request) {
        return ResponseEntity.ok(
                attractionService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attractionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}