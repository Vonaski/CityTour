package com.iksanov.citytour.tour.entity;

import com.iksanov.citytour.attraction.entity.Attraction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "tour_stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @Min(1)
    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Min(5)
    @Max(240)
    @Column(name = "stay_minutes", nullable = false)
    private Integer stayMinutes;
}