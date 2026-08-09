package com.iksanov.citytour.attraction.mapper;

import com.iksanov.citytour.attraction.dto.AttractionRequest;
import com.iksanov.citytour.attraction.dto.AttractionResponse;
import com.iksanov.citytour.attraction.entity.Attraction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AttractionMapper {

    @Mapping(target = "id", ignore = true)
    Attraction toEntity(AttractionRequest request);

    AttractionResponse toResponse(Attraction attraction);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
            AttractionRequest request,
            @MappingTarget Attraction attraction
    );
}