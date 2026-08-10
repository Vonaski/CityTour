package com.iksanov.citytour.booking.service;

import com.iksanov.citytour.attraction.entity.Attraction;
import com.iksanov.citytour.booking.dto.BookingCreateRequest;
import com.iksanov.citytour.booking.dto.BookingResponse;
import com.iksanov.citytour.booking.entity.Booking;
import com.iksanov.citytour.booking.entity.BookingStatus;
import com.iksanov.citytour.booking.mapper.BookingMapper;
import com.iksanov.citytour.booking.repository.BookingRepository;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.entity.TourStop;
import com.iksanov.citytour.tour.repository.TourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldThrow_whenTourDoesNotExist() {
        Long tourId = 999L;

        BookingCreateRequest request = new BookingCreateRequest(
                "Test Customer",
                "+998901234567",
                2
        );

        when(tourRepository.findByIdForUpdate(tourId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(tourId, request)
        );

        assertEquals("TOUR_NOT_FOUND", exception.getCode());

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenTourIsNotPublished() {
        Long tourId = 1L;

        Tour tour = createTour(
                TourStatus.DRAFT,
                10,
                new BigDecimal("100.00")
        );

        BookingCreateRequest request = new BookingCreateRequest(
                "Test Customer",
                "+998901234567",
                2
        );

        when(tourRepository.findByIdForUpdate(tourId))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(tourId, request)
        );

        assertEquals("TOUR_NOT_PUBLISHED", exception.getCode());

        verify(bookingRepository, never())
                .sumSeatsByTourIdAndStatus(anyLong(), any());

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrow_whenTourAlreadyStarted() {
        Long tourId = 1L;

        Tour tour = createTour(
                TourStatus.PUBLISHED,
                10,
                new BigDecimal("100.00")
        );

        tour.setStartTime(LocalDateTime.now().minusHours(1));

        BookingCreateRequest request = new BookingCreateRequest(
                "Test Customer",
                "+998901234567",
                2
        );

        when(tourRepository.findByIdForUpdate(tourId))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(tourId, request)
        );

        assertEquals("TOUR_ALREADY_STARTED", exception.getCode());

        verify(bookingRepository, never())
                .sumSeatsByTourIdAndStatus(anyLong(), any());

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrow_whenNotEnoughSeats() {
        Long tourId = 1L;

        Tour tour = createTour(
                TourStatus.PUBLISHED,
                5,
                new BigDecimal("100.00")
        );

        BookingCreateRequest request = new BookingCreateRequest(
                "Test Customer",
                "+998901234567",
                3
        );

        when(tourRepository.findByIdForUpdate(tourId))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                tourId,
                BookingStatus.CONFIRMED
        )).thenReturn(4);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(tourId, request)
        );

        assertEquals("NOT_ENOUGH_SEATS", exception.getCode());

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldCreateBooking_whenSeatsAreAvailable() {
        Long tourId = 1L;

        Tour tour = createTour(
                TourStatus.PUBLISHED,
                10,
                new BigDecimal("100.00")
        );

        BookingCreateRequest request = new BookingCreateRequest(
                "Test Customer",
                "+998901234567",
                2
        );

        Booking savedBooking = new Booking();

        when(tourRepository.findByIdForUpdate(tourId))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                tourId,
                BookingStatus.CONFIRMED
        )).thenReturn(3);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(savedBooking);

        BookingResponse response = mock(BookingResponse.class);

        when(bookingMapper.toResponse(savedBooking))
                .thenReturn(response);

        BookingResponse result = bookingService.create(tourId, request);

        assertSame(response, result);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking booking = captor.getValue();

        assertEquals(tour, booking.getTour());
        assertEquals("Test Customer", booking.getCustomerName());
        assertEquals("+998901234567", booking.getCustomerPhone());
        assertEquals(2, booking.getSeats());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void shouldCalculateTotalPrice_whenBookingCreated() {
        Long tourId = 1L;

        Tour tour = createTour(
                TourStatus.PUBLISHED,
                10,
                new BigDecimal("100.00")
        );

        Attraction firstAttraction = createAttraction(
                1L,
                new BigDecimal("50000.00")
        );

        Attraction secondAttraction = createAttraction(
                2L,
                new BigDecimal("30000.00")
        );

        TourStop firstStop = createStop(
                tour,
                firstAttraction,
                1,
                60
        );

        TourStop secondStop = createStop(
                tour,
                secondAttraction,
                2,
                60
        );

        tour.setStops(List.of(firstStop, secondStop));

        BookingCreateRequest request = new BookingCreateRequest(
                "Price Test",
                "+998901234567",
                2
        );

        when(tourRepository.findByIdForUpdate(tourId))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                tourId,
                BookingStatus.CONFIRMED
        )).thenReturn(0);

        Booking savedBooking = new Booking();

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(savedBooking);

        when(bookingMapper.toResponse(savedBooking))
                .thenReturn(mock(BookingResponse.class));

        bookingService.create(tourId, request);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking booking = captor.getValue();

        /*
         * pricePerSeat = 100
         * Registan entryFee = 50_000
         * Gur-e-Amir entryFee = 30_000
         * seats = 2
         *
         * 2 * (100 + 50_000 + 30_000)
         * = 160_200
         */
        assertEquals(
                new BigDecimal("160200.00"),
                booking.getTotalPrice()
        );
    }

    @Test
    void shouldCancelBooking_whenBookingExists() {
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.cancel(bookingId);

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getStatus()
        );
    }

    @Test
    void shouldThrow_whenBookingDoesNotExist() {
        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.cancel(bookingId)
        );

        assertEquals("BOOKING_NOT_FOUND", exception.getCode());
    }

    @Test
    void shouldDoNothing_whenBookingAlreadyCancelled() {
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.cancel(bookingId);

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getStatus()
        );
    }

    private Tour createTour(
            TourStatus status,
            int maxSeats,
            BigDecimal pricePerSeat
    ) {
        Tour tour = new Tour();

        tour.setStatus(status);
        tour.setMaxSeats(maxSeats);
        tour.setPricePerSeat(pricePerSeat);
        tour.setStartTime(
                LocalDateTime.now().plusDays(1)
        );

        return tour;
    }

    private Attraction createAttraction(
            Long id,
            BigDecimal entryFee
    ) {
        Attraction attraction = new Attraction();

        attraction.setId(id);
        attraction.setEntryFee(entryFee);

        return attraction;
    }

    private TourStop createStop(
            Tour tour,
            Attraction attraction,
            int visitOrder,
            int stayMinutes
    ) {
        return TourStop.builder()
                .tour(tour)
                .attraction(attraction)
                .visitOrder(visitOrder)
                .stayMinutes(stayMinutes)
                .build();
    }
}