package com.iksanov.citytour.tour.mapper;

import com.iksanov.citytour.guide.mapper.GuideMapper;
import com.iksanov.citytour.tour.dto.StopResponse;
import com.iksanov.citytour.tour.dto.TourResponse;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = GuideMapper.class
)
public interface TourMapper {

    @Mapping(target = "bookedSeats", ignore = true)
    @Mapping(target = "freeSeats", ignore = true)
    TourResponse toResponse(Tour tour);

    @Mapping(target = "attractionId", source = "attraction.id")
    @Mapping(target = "name", source = "attraction.name")
    @Mapping(target = "entryFee", source = "attraction.entryFee")
    StopResponse toStopResponse(TourStop stop);

    List<StopResponse> toStopResponseList(List<TourStop> stops);
}