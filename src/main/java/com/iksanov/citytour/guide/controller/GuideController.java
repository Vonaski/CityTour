package com.iksanov.citytour.guide.controller;

import com.iksanov.citytour.guide.dto.GuideCreateRequest;
import com.iksanov.citytour.guide.dto.GuideListResponse;
import com.iksanov.citytour.guide.dto.GuideResponse;
import com.iksanov.citytour.guide.dto.GuideUpdateRequest;
import com.iksanov.citytour.guide.entity.Language;
import com.iksanov.citytour.guide.service.GuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @PostMapping
    public ResponseEntity<GuideResponse> create(@Valid @RequestBody GuideCreateRequest request) {
        GuideResponse response = guideService.create(request);
        URI location = URI.create("/api/guides/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuideResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                guideService.getById(id)
        );
    }

    @GetMapping
    public ResponseEntity<GuideListResponse> getAll(@RequestParam(required = false) Boolean active, @RequestParam(required = false) Language language, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                guideService.getAll(
                        active,
                        language,
                        page,
                        size
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuideResponse> update(@PathVariable Long id, @Valid @RequestBody GuideUpdateRequest request) {
        return ResponseEntity.ok(
                guideService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        guideService.delete(id);
        return ResponseEntity.noContent().build();
    }
}