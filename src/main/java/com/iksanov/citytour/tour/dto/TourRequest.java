package com.iksanov.citytour.tour.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TourRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        @NotNull
        Long guideId,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime,

        @NotNull
        @Min(1)
        @Max(50)
        Integer maxSeats,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal pricePerSeat,

        @Valid
        List<StopRequest> stops
) {
}