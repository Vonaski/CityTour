package com.iksanov.citytour.booking.service;

import com.iksanov.citytour.booking.dto.BookingCreateRequest;
import com.iksanov.citytour.booking.dto.BookingResponse;
import com.iksanov.citytour.booking.entity.Booking;
import com.iksanov.citytour.booking.entity.BookingStatus;
import com.iksanov.citytour.booking.mapper.BookingMapper;
import com.iksanov.citytour.booking.repository.BookingRepository;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponse create(Long tourId, BookingCreateRequest request) {
        log.info("Creating booking: tourId={}, seats={}", tourId, request.seats());

        Tour tour = tourRepository.findByIdForUpdate(tourId)
                .orElseThrow(() -> new BusinessException("Tour not found: " + tourId, "TOUR_NOT_FOUND", HttpStatus.NOT_FOUND));

        validateBookingAllowed(tour);

        int confirmedSeats = bookingRepository
                .sumSeatsByTourIdAndStatus(tourId, BookingStatus.CONFIRMED);

        if (confirmedSeats + request.seats() > tour.getMaxSeats()) {
            log.warn("Not enough seats: tourId={}, maxSeats={}, confirmedSeats={}, requestedSeats={}",
                    tourId,
                    tour.getMaxSeats(),
                    confirmedSeats,
                    request.seats()
            );

            throw new BusinessException(
                    "Not enough seats available",
                    "NOT_ENOUGH_SEATS",
                    HttpStatus.CONFLICT
            );
        }

        BigDecimal totalPrice = calculateTotalPrice(
                tour,
                request.seats()
        );

        Booking booking = Booking.builder()
                .tour(tour)
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .seats(request.seats())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created: id={}, tourId={}, seats={}, totalPrice={}",
                savedBooking.getId(),
                tourId,
                request.seats(),
                totalPrice
        );

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAll(Long tourId, BookingStatus status) {
        log.debug("Getting bookings: tourId={}, status={}", tourId, status);

        List<Booking> bookings = status == null
                ? bookingRepository.findAllByTourId(tourId)
                : bookingRepository.findAllByTourIdAndStatus(
                        tourId,
                        status
                );

        return bookingMapper.toResponseList(bookings);
    }

    @Transactional
    public void cancel(Long id) {
        log.info("Cancelling booking: id={}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Booking not found: " + id,
                        "BOOKING_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);

        log.info("Booking cancelled: id={}", id);
    }

    @Transactional
    public void cancelConfirmedByTourId(Long tourId) {
        log.info("Cancelling confirmed bookings: tourId={}", tourId);

        List<Booking> confirmedBookings =
                bookingRepository.findAllByTourIdAndStatus(
                        tourId,
                        BookingStatus.CONFIRMED
                );

        confirmedBookings.forEach(
                booking -> booking.setStatus(BookingStatus.CANCELLED)
        );
    }

    private void validateBookingAllowed(Tour tour) {
        if (tour.getStatus() != TourStatus.PUBLISHED) {
            throw new BusinessException(
                    "Booking is allowed only for PUBLISHED tours",
                    "TOUR_NOT_PUBLISHED",
                    HttpStatus.CONFLICT
            );
        }

        if (!LocalDateTime.now().isBefore(tour.getStartTime())) {
            throw new BusinessException(
                    "Booking is not allowed after tour start time",
                    "TOUR_ALREADY_STARTED",
                    HttpStatus.CONFLICT
            );
        }
    }

    private BigDecimal calculateTotalPrice(Tour tour, int seats) {
        BigDecimal totalEntryFee = tour.getStops()
                .stream()
                .map(stop -> stop.getAttraction().getEntryFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pricePerSeat = tour.getPricePerSeat()
                .add(totalEntryFee);

        return pricePerSeat.multiply(
                BigDecimal.valueOf(seats)
        );
    }
}