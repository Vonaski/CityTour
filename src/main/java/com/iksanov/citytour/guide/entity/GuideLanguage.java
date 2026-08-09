package com.iksanov.citytour.guide.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guide_languages")
@Getter
@Setter
@NoArgsConstructor
public class GuideLanguage {

    @EmbeddedId
    private GuideLanguageId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("guideId")
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;
}