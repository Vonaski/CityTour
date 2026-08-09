package com.iksanov.citytour.attraction.dto;

import com.iksanov.citytour.attraction.entity.AttractionCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AttractionRequest {

    @NotBlank
    private String name;

    private String address;

    @NotNull
    @DecimalMin(value = "37.0")
    @DecimalMax(value = "46.0")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "55.0")
    @DecimalMax(value = "74.0")
    private Double longitude;

    @NotNull
    private AttractionCategory category;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal entryFee;
}