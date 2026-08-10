package com.iksanov.citytour.statistics.dto;

import com.iksanov.citytour.attraction.entity.AttractionCategory;

public record TopAttractionResponse(
        Long attractionId,
        String name,
        AttractionCategory category,
        Long tourCount,
        Long visitorCount
) {
}