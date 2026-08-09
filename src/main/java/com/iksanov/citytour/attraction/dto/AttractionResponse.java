package com.iksanov.citytour.attraction.dto;

import com.iksanov.citytour.attraction.entity.AttractionCategory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AttractionResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private AttractionCategory category;
    private BigDecimal entryFee;
}