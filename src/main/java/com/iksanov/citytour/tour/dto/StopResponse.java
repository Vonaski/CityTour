package com.iksanov.citytour.tour.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StopResponse {

    private Long attractionId;
    private String name;
    private Integer visitOrder;
    private Integer stayMinutes;
    private BigDecimal entryFee;
}