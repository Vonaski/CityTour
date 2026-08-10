package com.iksanov.citytour.tour.controller;

import com.iksanov.citytour.tour.dto.TourRequest;
import com.iksanov.citytour.tour.dto.TourResponse;
import com.iksanov.citytour.tour.dto.TourSummaryResponse;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @PostMapping
    public ResponseEntity<TourResponse> create(@Valid @RequestBody TourRequest request) {
        TourResponse response = tourService.create(request);

        URI location = URI.create("/api/tours/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                tourService.getById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<TourResponse>> getAll(
            @RequestParam(required = false) Long guideId,
            @RequestParam(required = false) TourStatus status,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                tourService.getAll(
                        guideId,
                        status,
                        dateFrom,
                        dateTo,
                        pageable
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponse> update(@PathVariable Long id, @Valid @RequestBody TourRequest request) {
        return ResponseEntity.ok(
                tourService.update(id, request)
        );
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<TourResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(
                tourService.publish(id)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TourResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                tourService.cancel(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<TourSummaryResponse> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(
                tourService.getSummary(id)
        );
    }
}