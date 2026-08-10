package com.iksanov.citytour.tour.dto;

import com.iksanov.citytour.attraction.dto.AttractionResponse;

public record StopResponse(
        Integer visitOrder,
        Integer stayMinutes,
        AttractionResponse attraction
) {
}