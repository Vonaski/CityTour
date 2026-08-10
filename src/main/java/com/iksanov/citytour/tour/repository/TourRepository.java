package com.iksanov.citytour.tour.repository;

import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long> {

    @Query("""
        SELECT t
        FROM Tour t
        WHERE (:guideId IS NULL OR t.guide.id = :guideId)
          AND (:status IS NULL OR t.status = :status)
          AND (:dateFrom IS NULL OR t.startTime >= :dateFrom)
          AND (:dateTo IS NULL OR t.startTime <= :dateTo)
        """)
    Page<Tour> findAllByFilters(
            @Param("guideId") Long guideId,
            @Param("status") TourStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Tour t
            WHERE t.guide.id = :guideId
              AND t.status <> :cancelledStatus
              AND t.startTime < :endTime
              AND t.endTime > :startTime
              AND (:tourId IS NULL OR t.id <> :tourId)
            """)
    Optional<Tour> findOverlappingTour(
            @Param("guideId") Long guideId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("cancelledStatus") TourStatus cancelledStatus,
            @Param("tourId") Long tourId
    );

    boolean existsByGuideIdAndStatusIn(
            Long guideId,
            List<TourStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM Tour t
            WHERE t.id = :id
            """)
    Optional<Tour> findByIdForUpdate(@Param("id") Long id);
}