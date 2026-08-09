package com.iksanov.citytour.tour.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TourRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long guideId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    @Min(1)
    @Max(50)
    private Integer maxSeats;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerSeat;

    @Valid
    private List<StopRequest> stops;

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) {
            return true;
        }

        return endTime.isAfter(startTime);
    }
}