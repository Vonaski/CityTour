package com.iksanov.citytour.tour.service;

import com.iksanov.citytour.attraction.entity.Attraction;
import com.iksanov.citytour.attraction.repository.AttractionRepository;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.guide.entity.Guide;
import com.iksanov.citytour.guide.repository.GuideRepository;
import com.iksanov.citytour.tour.dto.StopRequest;
import com.iksanov.citytour.tour.dto.TourRequest;
import com.iksanov.citytour.tour.dto.TourResponse;
import com.iksanov.citytour.tour.entity.Tour;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.entity.TourStop;
import com.iksanov.citytour.tour.mapper.TourMapper;
import com.iksanov.citytour.tour.repository.TourRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final GuideRepository guideRepository;
    private final AttractionRepository attractionRepository;
    private final TourMapper tourMapper;

    @Transactional
    public TourResponse create(TourRequest request) {
        Guide guide = guideRepository.findById(request.getGuideId())
                .orElseThrow(() -> new BusinessException(
                        "Guide not found: " + request.getGuideId(),
                        "GUIDE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        validateGuideAvailability(
                guide.getId(),
                request.getStartTime(),
                request.getEndTime(),
                null
        );

        Tour tour = Tour.builder()
                .title(request.getTitle())
                .guide(guide)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxSeats(request.getMaxSeats())
                .pricePerSeat(request.getPricePerSeat())
                .status(TourStatus.DRAFT)
                .build();

        setStops(tour, request.getStops());

        Tour savedTour = tourRepository.save(tour);

        return tourMapper.toResponse(savedTour);
    }

    @Transactional
    public TourResponse update(Long id, TourRequest request) {
        Tour tour = getTour(id);

        Guide guide = guideRepository.findById(request.getGuideId())
                .orElseThrow(() -> new BusinessException(
                        "Guide not found: " + request.getGuideId(),
                        "GUIDE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        validateGuideAvailability(
                guide.getId(),
                request.getStartTime(),
                request.getEndTime(),
                tour.getId()
        );

        tour.setTitle(request.getTitle());
        tour.setGuide(guide);
        tour.setStartTime(request.getStartTime());
        tour.setEndTime(request.getEndTime());
        tour.setMaxSeats(request.getMaxSeats());
        tour.setPricePerSeat(request.getPricePerSeat());

        tour.getStops().clear();
        setStops(tour, request.getStops());

        return tourMapper.toResponse(tour);
    }

    public TourResponse getById(Long id) {
        Tour tour = getTour(id);

        return tourMapper.toResponse(tour);
    }

    public Page<TourResponse> getAll(
            Long guideId,
            TourStatus status,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        return tourRepository.findAllByFilters(
                        guideId,
                        status,
                        dateFrom,
                        dateTo,
                        pageable
                )
                .map(tourMapper::toResponse);
    }

    @Transactional
    public TourResponse publish(Long id) {
        Tour tour = getTour(id);

        validatePublish(tour);

        tour.setStatus(TourStatus.PUBLISHED);

        return tourMapper.toResponse(tour);
    }

    private void validatePublish(Tour tour) {
        validateStopCount(tour);

        validateStopOrders(
                tour.getStops()
                        .stream()
                        .map(TourStop::getVisitOrder)
                        .toList()
        );

        validateTourDuration(tour);
    }

    private void validateStopCount(Tour tour) {
        if (tour.getStops().size() < 2) {
            throw new BusinessException(
                    "Tour must have at least 2 stops to be published",
                    "MIN_STOPS_REQUIRED",
                    HttpStatus.CONFLICT
            );
        }
    }

    private void validateStopOrders(List<Integer> orders) {
        Set<Integer> uniqueOrders = new HashSet<>(orders);

        if (uniqueOrders.size() != orders.size()) {
            throw new BusinessException(
                    "Duplicate visitOrder",
                    "DUPLICATE_VISIT_ORDER",
                    HttpStatus.CONFLICT
            );
        }

        int expectedOrder = 1;

        for (Integer order : uniqueOrders.stream().sorted().toList()) {
            if (order != expectedOrder) {
                throw new BusinessException(
                        "visitOrder must contain sequential values from 1 to N",
                        "INVALID_VISIT_ORDER",
                        HttpStatus.CONFLICT
                );
            }

            expectedOrder++;
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

        if (tourDurationMinutes < totalStayMinutes) {
            throw new BusinessException(
                    "Tour duration (" + tourDurationMinutes
                            + " min) is less than total stay time ("
                            + totalStayMinutes + " min)",
                    "INSUFFICIENT_TOUR_DURATION",
                    HttpStatus.CONFLICT
            );
        }
    }

    private void validateGuideAvailability(
            Long guideId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long tourId
    ) {
        tourRepository.findOverlappingTour(
                guideId,
                startTime,
                endTime,
                TourStatus.CANCELLED,
                tourId
        ).ifPresent(existingTour -> {
            throw new BusinessException(
                    "Guide " + guideId
                            + " already has an overlapping tour",
                    "GUIDE_TIME_OVERLAP",
                    HttpStatus.CONFLICT
            );
        });
    }

    private void setStops(Tour tour, List<StopRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        validateStopOrders(
                requests.stream()
                        .map(StopRequest::getVisitOrder)
                        .toList()
        );

        for (StopRequest request : requests) {
            Attraction attraction = attractionRepository
                    .findById(request.getAttractionId())
                    .orElseThrow(() -> new BusinessException(
                            "Attraction not found: "
                                    + request.getAttractionId(),
                            "ATTRACTION_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    ));

            TourStop stop = TourStop.builder()
                    .tour(tour)
                    .attraction(attraction)
                    .visitOrder(request.getVisitOrder())
                    .stayMinutes(request.getStayMinutes())
                    .build();

            tour.getStops().add(stop);
        }
    }

    private Tour getTour(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Tour not found: " + id,
                        "TOUR_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));
    }
}