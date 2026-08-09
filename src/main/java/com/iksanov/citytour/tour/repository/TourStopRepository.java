package com.iksanov.citytour.tour.repository;

import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.entity.TourStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface TourStopRepository extends JpaRepository<TourStop, Long> {

    @Query("""
            SELECT COUNT(ts) > 0
            FROM TourStop ts
            WHERE ts.attraction.id = :attractionId
              AND ts.tour.status IN :statuses
            """)
    boolean existsByAttractionIdAndTourStatusIn(
            @Param("attractionId") Long attractionId,
            @Param("statuses") Collection<TourStatus> statuses
    );
}