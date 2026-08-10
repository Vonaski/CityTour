package com.iksanov.citytour.statistics.repository;

import com.iksanov.citytour.tour.entity.Tour;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsRepository extends Repository<Tour, Long> {

    @Query(value = """
            SELECT
                t.id AS tour_id,
                t.guide_id AS guide_id,
                g.full_name AS guide_name,
                t.start_time AS start_time,
                t.end_time AS end_time,
                t.max_seats AS max_seats,
                COALESCE(
                    (
                        SELECT SUM(b.seats)
                        FROM bookings b
                        WHERE b.tour_id = t.id
                          AND b.status = 'CONFIRMED'
                    ),
                    0
                ) AS seats_sold,
                COALESCE(
                    (
                        SELECT SUM(b.total_price)
                        FROM bookings b
                        WHERE b.tour_id = t.id
                          AND b.status = 'CONFIRMED'
                    ),
                    0
                ) AS total_revenue
            FROM tours t
            JOIN guides g ON g.id = t.guide_id
            WHERE t.status = 'PUBLISHED'
              AND t.start_time >= :from
              AND t.start_time < :to
            """,
            nativeQuery = true)
    List<Object[]> findPublishedToursForStatistics(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT
                t.guide_id AS guide_id,
                a.category AS category,
                COUNT(*) AS category_count
            FROM tours t
            JOIN tour_stops ts ON ts.tour_id = t.id
            JOIN attractions a ON a.id = ts.attraction_id
            WHERE t.status = 'PUBLISHED'
              AND t.start_time >= :from
              AND t.start_time < :to
            GROUP BY t.guide_id, a.category
            """,
            nativeQuery = true)
    List<Object[]> findGuideCategories(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
            SELECT
                a.id AS attraction_id,
                a.name AS name,
                a.category AS category,
                COUNT(DISTINCT t.id) AS tour_count,
                COALESCE(SUM(
                    CASE
                        WHEN b.status = 'CONFIRMED' THEN b.seats
                        ELSE 0
                    END
                ), 0) AS visitor_count
            FROM attractions a
            JOIN tour_stops ts ON ts.attraction_id = a.id
            JOIN tours t ON t.id = ts.tour_id
            LEFT JOIN bookings b ON b.tour_id = t.id
            WHERE t.status = 'PUBLISHED'
              AND t.start_time >= :from
              AND t.start_time < :to
            GROUP BY a.id, a.name, a.category
            ORDER BY visitor_count DESC, name ASC
            """,
            nativeQuery = true)
    List<Object[]> findTopAttractions(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}