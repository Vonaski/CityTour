package com.iksanov.citytour.attraction.service;

import com.iksanov.citytour.attraction.dto.AttractionRequest;
import com.iksanov.citytour.attraction.dto.AttractionResponse;
import com.iksanov.citytour.attraction.entity.Attraction;
import com.iksanov.citytour.attraction.entity.AttractionCategory;
import com.iksanov.citytour.attraction.mapper.AttractionMapper;
import com.iksanov.citytour.attraction.repository.AttractionRepository;
import com.iksanov.citytour.common.exception.AttractionNotFoundException;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.repository.TourStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final AttractionMapper attractionMapper;
    private final TourStopRepository tourStopRepository;

    @Transactional
    public AttractionResponse create(AttractionRequest request) {
        log.info("Creating attraction: name={}", request.name());
        Attraction attraction = attractionMapper.toEntity(request);
        Attraction savedAttraction = attractionRepository.save(attraction);
        log.info("Attraction created: id={}", savedAttraction.getId());
        return attractionMapper.toResponse(savedAttraction);
    }

    public AttractionResponse getById(Long id) {
        log.debug("Getting attraction: id={}", id);
        Attraction attraction = getAttraction(id);
        return attractionMapper.toResponse(attraction);
    }

    public Page<AttractionResponse> getAll(AttractionCategory category, String search, Pageable pageable) {
        log.debug("Getting attractions: category={}, search={}, page={}, size={}", category, search, pageable.getPageNumber(), pageable.getPageSize());

        Page<Attraction> attractions;
        if (category != null && search != null && !search.isBlank()) {
            attractions = attractionRepository.findAllByCategoryAndNameContainingIgnoreCase(category, search, pageable);
        } else if (category != null) {
            attractions = attractionRepository.findAllByCategory(category, pageable);
        } else if (search != null && !search.isBlank()) {
            attractions = attractionRepository.findAllByNameContainingIgnoreCase(search, pageable);
        } else {
            attractions = attractionRepository.findAll(pageable);
        }
        return attractions.map(attractionMapper::toResponse);
    }

    @Transactional
    public AttractionResponse update(Long id, AttractionRequest request) {
        log.info("Updating attraction: id={}", id);
        Attraction attraction = getAttraction(id);
        attractionMapper.updateEntity(request, attraction);
        log.info("Attraction updated: id={}", id);
        return attractionMapper.toResponse(attraction);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting attraction: id={}", id);
        Attraction attraction = getAttraction(id);
        boolean usedByActiveTour =
                tourStopRepository.existsByAttractionIdAndTourStatusIn(id,
                        List.of(
                                TourStatus.DRAFT,
                                TourStatus.PUBLISHED
                        )
                );

        if (usedByActiveTour) {
            log.warn("Cannot delete attraction: id={} is used by DRAFT or PUBLISHED tour", id);
            throw new BusinessException(
                    "Cannot delete attraction used by an active tour",
                    "ATTRACTION_USED_BY_ACTIVE_TOUR",
                    HttpStatus.CONFLICT
            );
        }

        attractionRepository.delete(attraction);
        log.info("Attraction deleted: id={}", id);
    }

    private Attraction getAttraction(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new AttractionNotFoundException(id));
    }
}