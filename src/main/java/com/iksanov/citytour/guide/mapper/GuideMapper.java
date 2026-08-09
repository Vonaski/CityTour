package com.iksanov.citytour.guide.mapper;

import com.iksanov.citytour.guide.dto.GuideCreateRequest;
import com.iksanov.citytour.guide.dto.GuideResponse;
import com.iksanov.citytour.guide.dto.GuideUpdateRequest;
import com.iksanov.citytour.guide.entity.Guide;
import com.iksanov.citytour.guide.entity.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GuideMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "languages", ignore = true)
    Guide toEntity(GuideCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "languages", ignore = true)
    void updateEntity(
            GuideUpdateRequest request,
            @MappingTarget Guide guide
    );

    @Mapping(target = "languages", expression = "java(getLanguages(guide))")
    GuideResponse toResponse(Guide guide);

    List<GuideResponse> toResponseList(List<Guide> guides);

    default Set<Language> getLanguages(Guide guide) {
        if (guide == null || guide.getLanguages() == null) {
            return Set.of();
        }

        return guide.getLanguages()
                .stream()
                .map(guideLanguage -> guideLanguage.getId().getLanguage())
                .collect(Collectors.toSet());
    }
}