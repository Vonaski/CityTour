package com.iksanov.citytour.tour.entity;

import com.iksanov.citytour.guide.entity.Guide;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    @Column(name = "price_per_seat", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TourStatus status = TourStatus.DRAFT;

    @OneToMany(
            mappedBy = "tour",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("visitOrder ASC")
    @Builder.Default
    private List<TourStop> stops = new ArrayList<>();
}