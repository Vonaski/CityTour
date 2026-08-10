package com.iksanov.citytour.guide.dto;

import com.iksanov.citytour.guide.entity.Language;

import java.util.Set;

public record GuideResponse(
        Long id,
        String fullName,
        String phone,
        Set<Language> languages,
        Integer experienceYears,
        Boolean active
) {
}