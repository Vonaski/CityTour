package com.iksanov.citytour.tour.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StopRequest {

    @NotNull
    private Long attractionId;

    @NotNull
    @Min(1)
    private Integer visitOrder;

    @NotNull
    @Min(5)
    @Max(240)
    private Integer stayMinutes;
}