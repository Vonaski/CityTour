package com.iksanov.citytour.tour.service;

import com.iksanov.citytour.attraction.entity.Attraction;
import com.iksanov.citytour.attraction.repository.AttractionRepository;
import com.iksanov.citytour.booking.entity.Booking;
import com.iksanov.citytour.booking.entity.BookingStatus;
import com.iksanov.citytour.booking.repository.BookingRepository;
import com.iksanov.citytour.booking.service.BookingService;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.guide.entity.Guide;
import com.iksanov.citytour.guide.repository.GuideRepository;
import com.iksanov.citytour.tour.dto.StopRequest;
import com.iksanov.citytour.tour.dto.StopResponse;
import com.iksanov.citytour.tour.dto.TourRequest;
import com.iksanov.citytour.tour.dto.TourResponse;
import com.iksanov.citytour.tour.dto.TourSummaryResponse;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.entity.TourStop;
import com.iksanov.citytour.tour.mapper.TourMapper;
import com.iksanov.citytour.tour.repository.TourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AttractionRepository attractionRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private TourService tourService;

    @Test
    void shouldCreateTour_whenRequestIsValid() {
        TourRequest request = validRequest(List.of());

        Guide guide = guide(1L);

        Tour tour = Tour.builder()
                .id(10L)
                .title("Tashkent City Tour")
                .guide(guide)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .maxSeats(request.maxSeats())
                .pricePerSeat(request.pricePerSeat())
                .status(TourStatus.DRAFT)
                .build();

        TourResponse response = response(tour, 0, 20);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                1L,
                request.startTime(),
                request.endTime(),
                TourStatus.CANCELLED,
                null
        )).thenReturn(false);

        when(tourMapper.toEntity(request))
                .thenReturn(tour);

        when(tourRepository.save(tour))
                .thenReturn(tour);

        when(tourMapper.toResponse(tour))
                .thenReturn(response);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(0);

        TourResponse result = tourService.create(request);

        assertEquals(10L, result.id());
        assertEquals("Tashkent City Tour", result.title());
        assertEquals(TourStatus.DRAFT, result.status());
        assertEquals(20, result.maxSeats());
        assertEquals(0, result.bookedSeats());
        assertEquals(20, result.freeSeats());

        verify(tourRepository).save(tour);
    }

    @Test
    void shouldCreateTourWithStops_whenRequestContainsStops() {
        StopRequest firstStop = new StopRequest(100L, 1, 60);
        StopRequest secondStop = new StopRequest(200L, 2, 45);

        TourRequest request = validRequest(
                List.of(firstStop, secondStop)
        );

        Guide guide = guide(1L);

        Tour tour = Tour.builder()
                .id(10L)
                .guide(guide)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .maxSeats(20)
                .pricePerSeat(new BigDecimal("150000"))
                .status(TourStatus.DRAFT)
                .build();

        Attraction firstAttraction = attraction(100L);
        Attraction secondAttraction = attraction(200L);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                eq(1L),
                eq(request.startTime()),
                eq(request.endTime()),
                eq(TourStatus.CANCELLED),
                eq(null)
        )).thenReturn(false);

        when(tourMapper.toEntity(request))
                .thenReturn(tour);

        when(attractionRepository.findById(100L))
                .thenReturn(Optional.of(firstAttraction));

        when(attractionRepository.findById(200L))
                .thenReturn(Optional.of(secondAttraction));

        when(tourRepository.save(tour))
                .thenReturn(tour);

        when(tourMapper.toResponse(tour))
                .thenReturn(response(tour, 0, 20));

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(0);

        tourService.create(request);

        assertEquals(2, tour.getStops().size());
        assertEquals(100L, tour.getStops().get(0).getAttraction().getId());
        assertEquals(1, tour.getStops().get(0).getVisitOrder());
        assertEquals(45, tour.getStops().get(1).getStayMinutes());

        verify(attractionRepository).findById(100L);
        verify(attractionRepository).findById(200L);
    }

    @Test
    void shouldThrow_whenGuideDoesNotExist() {
        TourRequest request = validRequest(List.of());

        when(guideRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.create(request)
        );

        assertEquals("GUIDE_NOT_FOUND", exception.getCode());

        verifyNoInteractions(tourRepository);
        verifyNoInteractions(attractionRepository);
    }

    @Test
    void shouldThrow_whenTourTimeRangeIsInvalid() {
        TourRequest request = new TourRequest(
                "Invalid Tour",
                1L,
                LocalDateTime.of(2026, 8, 10, 15, 0),
                LocalDateTime.of(2026, 8, 10, 10, 0),
                20,
                new BigDecimal("100000"),
                List.of()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.create(request)
        );

        assertEquals("INVALID_TIME_RANGE", exception.getCode());

        verifyNoInteractions(guideRepository);
        verifyNoInteractions(tourRepository);
    }

    @Test
    void shouldThrow_whenGuideHasOverlappingTour() {
        TourRequest request = validRequest(List.of());

        Guide guide = guide(1L);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                1L,
                request.startTime(),
                request.endTime(),
                TourStatus.CANCELLED,
                null
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.create(request)
        );

        assertEquals(
                "GUIDE_HAS_OVERLAPPING_TOUR",
                exception.getCode()
        );

        verify(tourRepository, never()).save(any());
        verifyNoInteractions(tourMapper);
    }

    @Test
    void shouldThrow_whenDuplicateAttractionInStops() {
        StopRequest first = new StopRequest(100L, 1, 60);
        StopRequest second = new StopRequest(100L, 2, 60);

        TourRequest request = validRequest(
                List.of(first, second)
        );

        Guide guide = guide(1L);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                any(),
                any(),
                any(),
                eq(TourStatus.CANCELLED),
                eq(null)
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.create(request)
        );

        assertEquals(
                "TOUR_DUPLICATE_ATTRACTION",
                exception.getCode()
        );

        verifyNoInteractions(tourMapper);
        verifyNoInteractions(attractionRepository);
        verify(tourRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenDuplicateVisitOrderInStops() {
        StopRequest first = new StopRequest(100L, 1, 60);
        StopRequest second = new StopRequest(200L, 1, 60);

        TourRequest request = validRequest(
                List.of(first, second)
        );

        Guide guide = guide(1L);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                any(),
                any(),
                any(),
                eq(TourStatus.CANCELLED),
                eq(null)
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.create(request)
        );

        assertEquals(
                "TOUR_DUPLICATE_VISIT_ORDER",
                exception.getCode()
        );

        verifyNoInteractions(tourMapper);
        verifyNoInteractions(attractionRepository);
        verify(tourRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenAttractionDoesNotExist() {
        StopRequest stop = new StopRequest(100L, 1, 60);

        TourRequest request = validRequest(
                List.of(stop)
        );

        Guide guide = guide(1L);

        Tour tour = Tour.builder()
                .id(10L)
                .guide(guide)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .maxSeats(20)
                .pricePerSeat(request.pricePerSeat())
                .status(TourStatus.DRAFT)
                .build();

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                any(),
                any(),
                any(),
                eq(TourStatus.CANCELLED),
                eq(null)
        )).thenReturn(false);

        when(tourMapper.toEntity(request))
                .thenReturn(tour);

        when(attractionRepository.findById(100L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.create(request)
        );

        assertEquals(
                "ATTRACTION_NOT_FOUND",
                exception.getCode()
        );

        verify(tourRepository, never()).save(any());
    }

    @Test
    void shouldReturnTour_whenGetById() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourResponse expected = response(tour, 5, 15);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(tourMapper.toResponse(tour))
                .thenReturn(expected);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(5);

        TourResponse result = tourService.getById(10L);

        assertEquals(10L, result.id());
        assertEquals(5, result.bookedSeats());
        assertEquals(15, result.freeSeats());
    }

    @Test
    void shouldThrow_whenGetByIdTourDoesNotExist() {
        when(tourRepository.findById(10L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.getById(10L)
        );

        assertEquals("TOUR_NOT_FOUND", exception.getCode());

        verifyNoInteractions(tourMapper);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void shouldReturnAllTours_withFiltersAndPagination() {
        Pageable pageable = PageRequest.of(1, 10);

        Tour firstTour = tour(1L, TourStatus.DRAFT);
        Tour secondTour = tour(2L, TourStatus.PUBLISHED);

        Page<Tour> page = new PageImpl<>(
                List.of(firstTour, secondTour),
                pageable,
                12
        );

        TourResponse firstResponse =
                response(firstTour, 0, 20);

        TourResponse secondResponse =
                response(secondTour, 10, 10);

        when(tourRepository.findAllByFilters(
                1L,
                TourStatus.PUBLISHED,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59),
                pageable
        )).thenReturn(page);

        when(tourMapper.toResponse(firstTour))
                .thenReturn(firstResponse);

        when(tourMapper.toResponse(secondTour))
                .thenReturn(secondResponse);

        Page<TourResponse> result = tourService.getAll(
                1L,
                TourStatus.PUBLISHED,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59),
                pageable
        );

        assertEquals(2, result.getContent().size());
        assertEquals(12, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(10, result.getSize());
    }

    @Test
    void shouldReturnTourSummary() {
        Tour tour = Tour.builder()
                .id(10L)
                .maxSeats(20)
                .startTime(LocalDateTime.of(2026, 8, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 8, 10, 13, 0))
                .stops(new ArrayList<>())
                .build();

        TourStop firstStop = new TourStop();
        firstStop.setStayMinutes(60);

        TourStop secondStop = new TourStop();
        secondStop.setStayMinutes(45);

        tour.getStops().add(firstStop);
        tour.getStops().add(secondStop);

        Booking firstBooking = new Booking();
        firstBooking.setTotalPrice(new BigDecimal("300000"));

        Booking secondBooking = new Booking();
        secondBooking.setTotalPrice(new BigDecimal("450000"));

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(8);

        when(bookingRepository.findAllByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(List.of(firstBooking, secondBooking));

        TourSummaryResponse result =
                tourService.getSummary(10L);

        assertEquals(12, result.freeSeats());
        assertEquals(8, result.bookedSeats());
        assertEquals(
                new BigDecimal("40.0"),
                result.occupancyRate()
        );
        assertEquals(
                new BigDecimal("750000"),
                result.totalRevenue()
        );
        assertEquals(105, result.totalStayMinutes());
        assertEquals(2, result.stopsCount());
    }

    @Test
    void shouldThrow_whenUpdatingNonDraftTour() {
        Tour tour = tour(10L, TourStatus.PUBLISHED);

        TourRequest request = validRequest(List.of());

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.update(10L, request)
        );

        assertEquals(
                "TOUR_CANNOT_BE_UPDATED",
                exception.getCode()
        );

        verifyNoInteractions(guideRepository);
        verifyNoInteractions(tourMapper);
    }

    @Test
    void shouldThrow_whenUpdatingWithBookedSeatsGreaterThanMaxSeats() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourRequest request = new TourRequest(
                "Updated Tour",
                1L,
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 10, 14, 0),
                5,
                new BigDecimal("200000"),
                List.of()
        );

        Guide guide = guide(1L);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                1L,
                request.startTime(),
                request.endTime(),
                TourStatus.CANCELLED,
                10L
        )).thenReturn(false);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(6);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.update(10L, request)
        );

        assertEquals(
                "TOUR_MAX_SEATS_TOO_LOW",
                exception.getCode()
        );

        verify(tourMapper, never()).updateEntity(any(), any());
    }

    @Test
    void shouldUpdateTour_whenTourIsDraft() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourRequest request = new TourRequest(
                "Updated Tour",
                2L,
                LocalDateTime.of(2026, 8, 10, 11, 0),
                LocalDateTime.of(2026, 8, 10, 15, 0),
                30,
                new BigDecimal("200000"),
                List.of()
        );

        Guide guide = guide(2L);

        TourResponse mappedResponse =
                response(tour, 5, 25);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(guideRepository.findById(2L))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsOverlappingTour(
                2L,
                request.startTime(),
                request.endTime(),
                TourStatus.CANCELLED,
                10L
        )).thenReturn(false);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(5);

        when(tourMapper.toResponse(tour))
                .thenReturn(mappedResponse);

        tourService.update(10L, request);

        verify(tourMapper).updateEntity(request, tour);
        verify(tourRepository).flush();

        assertEquals(guide, tour.getGuide());
        assertEquals(0, tour.getStops().size());
    }

    @Test
    void shouldThrow_whenPublishingNonDraftTour() {
        Tour tour = tour(10L, TourStatus.PUBLISHED);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.publish(10L)
        );

        assertEquals(
                "TOUR_CANNOT_BE_PUBLISHED",
                exception.getCode()
        );
    }

    @Test
    void shouldThrow_whenPublishingTourWithLessThanTwoStops() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourStop stop = stop(1, 60, 100L);

        tour.setStops(
                new ArrayList<>(List.of(stop))
        );

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.publish(10L)
        );

        assertEquals(
                "TOUR_MIN_STOPS_NOT_MET",
                exception.getCode()
        );
    }

    @Test
    void shouldThrow_whenPublishingTourWithDuplicateVisitOrders() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourStop first = stop(1, 60, 100L);
        TourStop second = stop(1, 60, 200L);

        tour.setStops(
                new ArrayList<>(List.of(first, second))
        );

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.publish(10L)
        );

        assertEquals(
                "TOUR_DUPLICATE_VISIT_ORDER",
                exception.getCode()
        );
    }

    @Test
    void shouldThrow_whenPublishingTourWithNonSequentialVisitOrders() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourStop first = stop(1, 60, 100L);
        TourStop second = stop(3, 60, 200L);

        tour.setStops(
                new ArrayList<>(List.of(first, second))
        );

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.publish(10L)
        );

        assertEquals(
                "TOUR_INVALID_VISIT_ORDER",
                exception.getCode()
        );
    }

    @Test
    void shouldThrow_whenPublishingTourWithDuplicateAttractions() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourStop first = stop(1, 60, 100L);
        TourStop second = stop(2, 60, 100L);

        tour.setStops(
                new ArrayList<>(List.of(first, second))
        );

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.publish(10L)
        );

        assertEquals(
                "TOUR_DUPLICATE_ATTRACTION",
                exception.getCode()
        );
    }

    @Test
    void shouldThrow_whenPublishingTourDurationIsTooShort() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        tour.setStartTime(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        tour.setEndTime(
                LocalDateTime.of(2026, 8, 10, 11, 0)
        );

        TourStop first = stop(1, 40, 100L);
        TourStop second = stop(2, 40, 200L);

        tour.setStops(
                new ArrayList<>(List.of(first, second))
        );

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.publish(10L)
        );

        assertEquals(
                "TOUR_DURATION_TOO_SHORT",
                exception.getCode()
        );
    }

    @Test
    void shouldPublishTour_whenAllRulesAreValid() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        tour.setStartTime(
                LocalDateTime.of(2026, 8, 10, 10, 0)
        );

        tour.setEndTime(
                LocalDateTime.of(2026, 8, 10, 14, 0)
        );

        TourStop first = stop(1, 60, 100L);
        TourStop second = stop(2, 45, 200L);

        tour.setStops(
                new ArrayList<>(List.of(first, second))
        );

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(tourMapper.toResponse(tour))
                .thenAnswer(invocation ->
                        response(tour, 5, 15)
                );

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(5);

        TourResponse result =
                tourService.publish(10L);

        assertEquals(TourStatus.PUBLISHED, tour.getStatus());
        assertEquals(TourStatus.PUBLISHED, result.status());
    }

    @Test
    void shouldCancelTour_andCancelConfirmedBookings() {
        Tour tour = tour(10L, TourStatus.PUBLISHED);

        TourResponse mappedResponse =
                response(tour, 5, 15);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(tourMapper.toResponse(tour))
                .thenReturn(mappedResponse);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(5);

        TourResponse result =
                tourService.cancel(10L);

        assertEquals(TourStatus.CANCELLED, tour.getStatus());
        assertEquals(TourStatus.PUBLISHED, result.status());

        verify(bookingService)
                .cancelConfirmedByTourId(10L);
    }

    @Test
    void shouldNotCancelBookings_whenTourAlreadyCancelled() {
        Tour tour = tour(10L, TourStatus.CANCELLED);

        TourResponse mappedResponse =
                response(tour, 0, 20);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(tourMapper.toResponse(tour))
                .thenReturn(mappedResponse);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(0);

        tourService.cancel(10L);

        verify(bookingService, never())
                .cancelConfirmedByTourId(10L);
    }

    @Test
    void shouldDeleteDraftTour_whenThereAreNoBookings() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.existsByTourId(10L))
                .thenReturn(false);

        tourService.delete(10L);

        verify(tourRepository).delete(tour);
    }

    @Test
    void shouldThrow_whenDeletingNonDraftTour() {
        Tour tour = tour(10L, TourStatus.PUBLISHED);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.delete(10L)
        );

        assertEquals(
                "TOUR_CANNOT_BE_DELETED",
                exception.getCode()
        );

        verify(bookingRepository, never())
                .existsByTourId(any());

        verify(tourRepository, never())
                .delete(any());
    }

    @Test
    void shouldThrow_whenDeletingTourWithBookings() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(bookingRepository.existsByTourId(10L))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tourService.delete(10L)
        );

        assertEquals(
                "TOUR_HAS_BOOKINGS",
                exception.getCode()
        );

        verify(tourRepository, never())
                .delete(any());
    }

    @Test
    void shouldReturnZeroBookedSeats_whenRepositoryReturnsNull() {
        Tour tour = tour(10L, TourStatus.DRAFT);

        TourResponse mappedResponse =
                response(tour, null, null);

        when(tourRepository.findById(10L))
                .thenReturn(Optional.of(tour));

        when(tourMapper.toResponse(tour))
                .thenReturn(mappedResponse);

        when(bookingRepository.sumSeatsByTourIdAndStatus(
                10L,
                BookingStatus.CONFIRMED
        )).thenReturn(null);

        TourResponse result =
                tourService.getById(10L);

        assertEquals(0, result.bookedSeats());
        assertEquals(20, result.freeSeats());
    }

    private TourRequest validRequest(List<StopRequest> stops) {
        return new TourRequest(
                "Tashkent City Tour",
                1L,
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 10, 14, 0),
                20,
                new BigDecimal("150000"),
                stops
        );
    }

    private Guide guide(Long id) {
        Guide guide = new Guide();
        guide.setId(id);
        guide.setFullName("Test Guide");
        guide.setPhone("+998901234567");
        guide.setExperienceYears(5);
        guide.setActive(true);
        return guide;
    }

    private Tour tour(Long id, TourStatus status) {
        return Tour.builder()
                .id(id)
                .title("Test Tour")
                .startTime(LocalDateTime.of(2026, 8, 10, 10, 0))
                .endTime(LocalDateTime.of(2026, 8, 10, 14, 0))
                .maxSeats(20)
                .pricePerSeat(new BigDecimal("150000"))
                .status(status)
                .stops(new ArrayList<>())
                .build();
    }

    private Attraction attraction(Long id) {
        Attraction attraction = new Attraction();
        attraction.setId(id);
        attraction.setName("Attraction " + id);
        return attraction;
    }

    private TourStop stop(
            int visitOrder,
            int stayMinutes,
            long attractionId
    ) {
        TourStop stop = new TourStop();

        stop.setVisitOrder(visitOrder);
        stop.setStayMinutes(stayMinutes);
        stop.setAttraction(attraction(attractionId));

        return stop;
    }

    private TourResponse response(
            Tour tour,
            Integer bookedSeats,
            Integer freeSeats
    ) {
        return new TourResponse(
                tour.getId(),
                tour.getTitle(),
                tour.getStatus(),
                null,
                tour.getStartTime(),
                tour.getEndTime(),
                tour.getMaxSeats(),
                bookedSeats,
                freeSeats,
                tour.getPricePerSeat(),
                List.<StopResponse>of()
        );
    }
}