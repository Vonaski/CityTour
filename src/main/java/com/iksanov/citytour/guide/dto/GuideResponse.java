package com.iksanov.citytour.guide.dto;

import com.iksanov.citytour.guide.entity.Language;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class GuideResponse {

    private Long id;
    private String fullName;
    private String phone;
    private Set<Language> languages;
    private Integer experienceYears;
    private Boolean active;
}