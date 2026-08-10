package com.iksanov.citytour.tour.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StopRequest(

        @NotNull
        Long attractionId,

        @NotNull
        @Min(1)
        Integer visitOrder,

        @NotNull
        @Min(5)
        @Max(240)
        Integer stayMinutes
) {
}