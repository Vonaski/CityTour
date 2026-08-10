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
import com.iksanov.citytour.tour.dto.TourRequest;
import com.iksanov.citytour.tour.dto.TourResponse;
import com.iksanov.citytour.tour.dto.TourSummaryResponse;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.entity.TourStop;
import com.iksanov.citytour.tour.mapper.TourMapper;
import com.iksanov.citytour.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourService {

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;
    private final GuideRepository guideRepository;
    private final BookingRepository bookingRepository;
    private final AttractionRepository attractionRepository;
    private final BookingService bookingService;

    @Transactional
    public TourResponse create(TourRequest request) {
        log.info(
                "Creating tour: guideId={}, title={}",
                request.guideId(),
                request.title()
        );

        validateTimeRange(request.startTime(), request.endTime());

        Guide guide = guideRepository.findById(request.guideId())
                .orElseThrow(() -> new BusinessException(
                        "Guide not found: " + request.guideId(),
                        "GUIDE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        validateGuideAvailability(
                request.guideId(),
                request.startTime(),
                request.endTime(),
                null
        );

        validateStopRequests(request.stops());

        Tour tour = tourMapper.toEntity(request);
        tour.setGuide(guide);
        tour.setStatus(TourStatus.DRAFT);

        addStops(tour, request.stops());

        Tour savedTour = tourRepository.save(tour);

        return buildResponse(savedTour);
    }

    @Transactional(readOnly = true)
    public TourResponse getById(Long id) {
        Tour tour = getTour(id);

        return buildResponse(tour);
    }

    @Transactional(readOnly = true)
    public Page<TourResponse> getAll(
            Long guideId,
            TourStatus status,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        log.debug(
                "Getting tours: guideId={}, status={}, dateFrom={}, dateTo={}, page={}, size={}",
                guideId,
                status,
                dateFrom,
                dateTo,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return tourRepository.findAllByFilters(
                        guideId,
                        status,
                        dateFrom,
                        dateTo,
                        pageable
                )
                .map(tourMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TourSummaryResponse getSummary(Long id) {
        Tour tour = getTour(id);

        Integer bookedSeats = bookingRepository.sumSeatsByTourIdAndStatus(id, BookingStatus.CONFIRMED);
        Integer freeSeats = tour.getMaxSeats() - bookedSeats;
        BigDecimal occupancyRate = BigDecimal.valueOf(bookedSeats)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(tour.getMaxSeats()), 1, RoundingMode.HALF_UP);

        BigDecimal totalRevenue = bookingRepository
                .findAllByTourIdAndStatus(id, BookingStatus.CONFIRMED)
                .stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalStayMinutes = tour.getStops()
                .stream()
                .mapToInt(TourStop::getStayMinutes)
                .sum();

        Integer stopsCount = tour.getStops().size();

        return new TourSummaryResponse(
                freeSeats,
                bookedSeats,
                occupancyRate,
                totalRevenue,
                totalStayMinutes,
                stopsCount
        );
    }

    @Transactional
    public TourResponse update(Long id, TourRequest request) {
        log.info("Updating tour: id={}", id);

        validateTimeRange(request.startTime(), request.endTime());

        Tour tour = getTour(id);

        if (tour.getStatus() != TourStatus.DRAFT) {
            throw new BusinessException(
                    "Only draft tours can be updated",
                    "TOUR_CANNOT_BE_UPDATED",
                    HttpStatus.CONFLICT
            );
        }

        Guide guide = guideRepository.findById(request.guideId())
                .orElseThrow(() -> new BusinessException(
                        "Guide not found: " + request.guideId(),
                        "GUIDE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        validateGuideAvailability(
                request.guideId(),
                request.startTime(),
                request.endTime(),
                id
        );

        Integer confirmedSeats = bookingRepository.sumSeatsByTourIdAndStatus(
                id,
                BookingStatus.CONFIRMED
        );

        if (confirmedSeats == null) {
            confirmedSeats = 0;
        }

        if (confirmedSeats > request.maxSeats()) {
            throw new BusinessException(
                    "Max seats cannot be less than already booked seats",
                    "TOUR_MAX_SEATS_TOO_LOW",
                    HttpStatus.CONFLICT
            );
        }

        validateStopRequests(request.stops());
        tourMapper.updateEntity(request, tour);
        tour.setGuide(guide);
        tour.getStops().clear();
        tourRepository.flush();
        addStops(tour, request.stops());
        return buildResponse(tour);
    }

    @Transactional
    public TourResponse publish(Long id) {
        log.info("Publishing tour: id={}", id);

        Tour tour = getTour(id);

        if (tour.getStatus() != TourStatus.DRAFT) {
            throw new BusinessException(
                    "Only draft tours can be published",
                    "TOUR_CANNOT_BE_PUBLISHED",
                    HttpStatus.CONFLICT
            );
        }

        validatePublish(tour);

        tour.setStatus(TourStatus.PUBLISHED);

        return buildResponse(tour);
    }

    @Transactional
    public TourResponse cancel(Long id) {
        log.info("Cancelling tour: id={}", id);

        Tour tour = getTour(id);

        if (tour.getStatus() == TourStatus.CANCELLED) {
            return buildResponse(tour);
        }

        tour.setStatus(TourStatus.CANCELLED);

        bookingService.cancelConfirmedByTourId(id);

        return buildResponse(tour);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting tour: id={}", id);

        Tour tour = getTour(id);

        if (tour.getStatus() != TourStatus.DRAFT) {
            throw new BusinessException(
                    "Only draft tours can be deleted",
                    "TOUR_CANNOT_BE_DELETED",
                    HttpStatus.CONFLICT
            );
        }

        if (bookingRepository.existsByTourId(id)) {
            throw new BusinessException(
                    "Cannot delete tour with bookings",
                    "TOUR_HAS_BOOKINGS",
                    HttpStatus.CONFLICT
            );
        }

        tourRepository.delete(tour);
    }

    private Tour getTour(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Tour not found: " + id,
                        "TOUR_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));
    }

    private TourResponse buildResponse(Tour tour) {
        TourResponse response = tourMapper.toResponse(tour);

        Integer bookedSeats = bookingRepository.sumSeatsByTourIdAndStatus(
                tour.getId(),
                BookingStatus.CONFIRMED
        );

        if (bookedSeats == null) {
            bookedSeats = 0;
        }

        int freeSeats = tour.getMaxSeats() - bookedSeats;

        return new TourResponse(
                response.id(),
                response.title(),
                response.status(),
                response.guide(),
                response.startTime(),
                response.endTime(),
                response.maxSeats(),
                bookedSeats,
                freeSeats,
                response.pricePerSeat(),
                response.stops()
        );
    }

    private void validateTimeRange(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(
                    "End time must be after start time",
                    "INVALID_TIME_RANGE",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateGuideAvailability(
            Long guideId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long tourId
    ) {
        boolean hasOverlap = tourRepository.existsOverlappingTour(
                guideId,
                startTime,
                endTime,
                TourStatus.CANCELLED,
                tourId
        );

        if (hasOverlap) {
            throw new BusinessException(
                    "Guide already has an overlapping tour",
                    "GUIDE_HAS_OVERLAPPING_TOUR",
                    HttpStatus.CONFLICT
            );
        }
    }

    private void validatePublish(Tour tour) {
        validateStopCount(tour);
        validateStopOrders(tour);
        validateUniqueAttractions(tour);
        validateTourDuration(tour);
    }

    private void validateStopCount(Tour tour) {
        if (tour.getStops() == null || tour.getStops().size() < 2) {
            throw new BusinessException(
                    "Published tour must contain at least 2 stops",
                    "TOUR_MIN_STOPS_NOT_MET",
                    HttpStatus.CONFLICT
            );
        }
    }

    private void validateStopOrders(Tour tour) {
        List<Integer> orders = tour.getStops()
                .stream()
                .map(TourStop::getVisitOrder)
                .toList();

        Set<Integer> uniqueOrders = new HashSet<>(orders);

        if (uniqueOrders.size() != orders.size()) {
            throw new BusinessException(
                    "Tour stop visit orders must be unique",
                    "TOUR_DUPLICATE_VISIT_ORDER",
                    HttpStatus.CONFLICT
            );
        }

        List<Integer> sortedOrders = orders.stream()
                .sorted()
                .toList();

        for (int i = 0; i < sortedOrders.size(); i++) {
            int expectedOrder = i + 1;

            if (sortedOrders.get(i) != expectedOrder) {
                throw new BusinessException(
                        "Tour stop visit orders must be sequential starting from 1",
                        "TOUR_INVALID_VISIT_ORDER",
                        HttpStatus.CONFLICT
                );
            }
        }
    }

    private void validateUniqueAttractions(Tour tour) {
        List<Long> attractionIds = tour.getStops()
                .stream()
                .map(stop -> stop.getAttraction().getId())
                .toList();

        Set<Long> uniqueAttractionIds = new HashSet<>(attractionIds);

        if (uniqueAttractionIds.size() != attractionIds.size()) {
            throw new BusinessException(
                    "An attraction cannot be used more than once in the same tour",
                    "TOUR_DUPLICATE_ATTRACTION",
                    HttpStatus.CONFLICT
            );
        }
    }

    private void validateTourDuration(Tour tour) {
        long totalStayMinutes = tour.getStops()
                .stream()
                .mapToLong(TourStop::getStayMinutes)
                .sum();

        long tourDurationMinutes = Duration.between(
                tour.getStartTime(),
                tour.getEndTime()
        ).toMinutes();

        if (totalStayMinutes > tourDurationMinutes) {
            throw new BusinessException(
                    "Total stop duration exceeds tour duration",
                    "TOUR_DURATION_TOO_SHORT",
                    HttpStatus.CONFLICT
            );
        }
    }

    private void addStops(Tour tour, List<StopRequest> stopRequests) {
        if (stopRequests == null || stopRequests.isEmpty()) {
            return;
        }

        List<TourStop> stops = new ArrayList<>();

        for (StopRequest request : stopRequests) {
            Attraction attraction = attractionRepository.findById(request.attractionId())
                    .orElseThrow(() -> new BusinessException(
                            "Attraction not found: " + request.attractionId(),
                            "ATTRACTION_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    ));

            TourStop stop = new TourStop();
            stop.setTour(tour);
            stop.setAttraction(attraction);
            stop.setVisitOrder(request.visitOrder());
            stop.setStayMinutes(request.stayMinutes());

            stops.add(stop);
        }

        tour.getStops().addAll(stops);
    }

    private void validateStopRequests(List<StopRequest> stops) {
        if (stops == null) {
            return;
        }

        Set<Long> attractionIds = new HashSet<>();
        Set<Integer> visitOrders = new HashSet<>();

        for (StopRequest stop : stops) {
            if (!attractionIds.add(stop.attractionId())) {
                throw new BusinessException(
                        "An attraction cannot be used more than once in the same tour",
                        "TOUR_DUPLICATE_ATTRACTION",
                        HttpStatus.CONFLICT
                );
            }

            if (!visitOrders.add(stop.visitOrder())) {
                throw new BusinessException(
                        "Tour stop visit orders must be unique",
                        "TOUR_DUPLICATE_VISIT_ORDER",
                        HttpStatus.CONFLICT
                );
            }
        }
    }
}