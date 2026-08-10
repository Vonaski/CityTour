package com.iksanov.citytour.attraction.dto;

import com.iksanov.citytour.attraction.entity.AttractionCategory;

import java.math.BigDecimal;

public record AttractionResponse(
        Long id,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        AttractionCategory category,
        BigDecimal entryFee
) {
}