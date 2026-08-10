package com.iksanov.citytour.tour.mapper;

import com.iksanov.citytour.tour.dto.StopResponse;
import com.iksanov.citytour.tour.dto.TourRequest;
import com.iksanov.citytour.tour.dto.TourResponse;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {
                com.iksanov.citytour.guide.mapper.GuideMapper.class,
                com.iksanov.citytour.attraction.mapper.AttractionMapper.class
        }
)
public interface TourMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "guide", ignore = true)
    @Mapping(target = "stops", ignore = true)
    Tour toEntity(TourRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "guide", ignore = true)
    @Mapping(target = "stops", ignore = true)
    void updateEntity(
            TourRequest request,
            @MappingTarget Tour tour
    );

    @Mapping(target = "bookedSeats", ignore = true)
    @Mapping(target = "freeSeats", ignore = true)
    TourResponse toResponse(Tour tour);

    StopResponse toStopResponse(TourStop stop);
}