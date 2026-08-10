package com.iksanov.citytour.guide.dto;

import com.iksanov.citytour.guide.entity.Language;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record GuideCreateRequest(

        @NotBlank
        @Size(min = 3, max = 120)
        String fullName,

        @NotBlank
        @Size(max = 20)
        String phone,

        @NotEmpty
        Set<Language> languages,

        @NotNull
        @Min(0)
        Integer experienceYears
) {
}