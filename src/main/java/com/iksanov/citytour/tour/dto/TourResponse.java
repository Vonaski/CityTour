package com.iksanov.citytour.tour.dto;

import com.iksanov.citytour.guide.dto.GuideResponse;
import com.iksanov.citytour.tour.entity.TourStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TourResponse(
        Long id,
        String title,
        TourStatus status,
        GuideResponse guide,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer maxSeats,
        Integer bookedSeats,
        Integer freeSeats,
        BigDecimal pricePerSeat,
        List<StopResponse> stops
) {
}