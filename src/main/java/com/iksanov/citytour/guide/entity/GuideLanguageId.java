package com.iksanov.citytour.guide.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GuideLanguageId implements Serializable {

    @Column(name = "guide_id")
    private Long guideId;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;
}