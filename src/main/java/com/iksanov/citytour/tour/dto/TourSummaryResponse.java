package com.iksanov.citytour.tour.dto;

import java.math.BigDecimal;

public record TourSummaryResponse(
        Integer freeSeats,
        Integer bookedSeats,
        BigDecimal occupancyRate,
        BigDecimal totalRevenue,
        Integer totalStayMinutes,
        Integer stopsCount
) {
}