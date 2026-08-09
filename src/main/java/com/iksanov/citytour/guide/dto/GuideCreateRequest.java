package com.iksanov.citytour.guide.dto;

import com.iksanov.citytour.guide.entity.Language;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class GuideCreateRequest {

    @NotBlank
    @Size(min = 3, max = 120)
    private String fullName;

    @NotBlank
    @Size(min = 9, max = 20)
    private String phone;

    @NotEmpty
    private Set<Language> languages;

    @Min(0)
    private Integer experienceYears;
}