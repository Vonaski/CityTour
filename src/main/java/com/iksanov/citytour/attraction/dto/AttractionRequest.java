package com.iksanov.citytour.attraction.dto;

import com.iksanov.citytour.attraction.entity.AttractionCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AttractionRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String address,

        @NotNull
        @DecimalMin("37.0")
        @DecimalMax("46.0")
        BigDecimal latitude,

        @NotNull
        @DecimalMin("55.0")
        @DecimalMax("74.0")
        BigDecimal longitude,

        @NotNull
        AttractionCategory category,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal entryFee
) {
}