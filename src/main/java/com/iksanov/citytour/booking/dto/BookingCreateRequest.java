package com.iksanov.citytour.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookingCreateRequest(

        @NotBlank
        @Size(max = 255)
        String customerName,

        @NotBlank
        @Size(max = 20)
        String customerPhone,

        @NotNull
        @Min(1)
        @Max(10)
        Integer seats
) {
}