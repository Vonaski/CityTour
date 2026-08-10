package com.iksanov.citytour.statistics.dto;

import com.iksanov.citytour.attraction.entity.AttractionCategory;

import java.math.BigDecimal;

public record GuideStatisticsResponse(
        Long guideId,
        String guideName,
        Long toursCount,
        BigDecimal totalHours,
        Long seatsSold,
        BigDecimal totalRevenue,
        BigDecimal occupancyRate,
        AttractionCategory topCategory
) {
}