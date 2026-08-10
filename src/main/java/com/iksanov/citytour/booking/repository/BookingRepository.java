package com.iksanov.citytour.booking.repository;

import com.iksanov.citytour.booking.entity.Booking;
import com.iksanov.citytour.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT COALESCE(SUM(b.seats), 0)
            FROM Booking b
            WHERE b.tour.id = :tourId
              AND b.status = :status
            """)
    Integer sumSeatsByTourIdAndStatus(
            @Param("tourId") Long tourId,
            @Param("status") BookingStatus status
    );

    List<Booking> findAllByTourId(Long tourId);

    List<Booking> findAllByTourIdAndStatus(
            Long tourId,
            BookingStatus status
    );

    boolean existsByTourId(Long tourId);
}