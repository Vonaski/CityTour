package com.iksanov.citytour.booking.dto;

import com.iksanov.citytour.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
        Long id,
        Long tourId,
        String customerName,
        String customerPhone,
        Integer seats,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant createdAt
) {
}