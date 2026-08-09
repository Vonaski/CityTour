package com.iksanov.citytour.tour.dto;

import com.iksanov.citytour.guide.dto.GuideResponse;
import com.iksanov.citytour.tour.entity.TourStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TourResponse {

    private Long id;
    private String title;
    private TourStatus status;
    private GuideResponse guide;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxSeats;
    private Integer bookedSeats;
    private Integer freeSeats;
    private BigDecimal pricePerSeat;
    private List<StopResponse> stops;
}