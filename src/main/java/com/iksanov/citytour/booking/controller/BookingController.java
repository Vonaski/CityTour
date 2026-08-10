package com.iksanov.citytour.booking.controller;

import com.iksanov.citytour.booking.dto.BookingCreateRequest;
import com.iksanov.citytour.booking.dto.BookingResponse;
import com.iksanov.citytour.booking.entity.BookingStatus;
import com.iksanov.citytour.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/tours/{tourId}/bookings")
    public ResponseEntity<BookingResponse> create(@PathVariable Long tourId, @Valid @RequestBody BookingCreateRequest request) {
        BookingResponse response = bookingService.create(tourId, request);
        URI location = URI.create("/api/bookings/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/tours/{tourId}/bookings")
    public ResponseEntity<List<BookingResponse>> getAll(@PathVariable Long tourId, @RequestParam(required = false) BookingStatus status) {
        return ResponseEntity.ok(
                bookingService.getAll(tourId, status)
        );
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}