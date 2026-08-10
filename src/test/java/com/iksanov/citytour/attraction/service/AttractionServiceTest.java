package com.iksanov.citytour.attraction.service;

import com.iksanov.citytour.attraction.entity.Attraction;
import com.iksanov.citytour.attraction.mapper.AttractionMapper;
import com.iksanov.citytour.attraction.repository.AttractionRepository;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.tour.repository.TourStopRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttractionServiceTest {

    @Mock
    private AttractionRepository attractionRepository;

    @Mock
    private AttractionMapper attractionMapper;

    @Mock
    private TourStopRepository tourStopRepository;

    @InjectMocks
    private AttractionService attractionService;

    @Test
    void shouldThrow_whenAttractionIsUsedByActiveTour() {
        Long attractionId = 1L;
        Attraction attraction = new Attraction();

        when(attractionRepository.findById(attractionId))
                .thenReturn(Optional.of(attraction));

        when(tourStopRepository.existsByAttractionIdAndTourStatusIn(
                eq(attractionId),
                anyList()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attractionService.delete(attractionId)
        );

        assertEquals("ATTRACTION_USED_BY_ACTIVE_TOUR", exception.getCode());

        verify(attractionRepository, never()).delete(any());
    }

    @Test
    void shouldDelete_whenAttractionIsNotUsedByActiveTour() {
        Long attractionId = 1L;
        Attraction attraction = new Attraction();

        when(attractionRepository.findById(attractionId))
                .thenReturn(Optional.of(attraction));

        when(tourStopRepository.existsByAttractionIdAndTourStatusIn(
                eq(attractionId),
                anyList()
        )).thenReturn(false);

        assertDoesNotThrow(() -> attractionService.delete(attractionId));

        verify(attractionRepository).delete(attraction);
    }

    @Test
    void shouldThrow_whenAttractionDoesNotExist() {
        Long attractionId = 999L;

        when(attractionRepository.findById(attractionId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> attractionService.delete(attractionId)
        );

        verify(tourStopRepository, never())
                .existsByAttractionIdAndTourStatusIn(anyLong(), anyList());

        verify(attractionRepository, never()).delete(any());
    }

    @Test
    void shouldCheckActiveTourStatuses_whenDeletingAttraction() {
        Long attractionId = 1L;
        Attraction attraction = new Attraction();

        when(attractionRepository.findById(attractionId))
                .thenReturn(Optional.of(attraction));

        when(tourStopRepository.existsByAttractionIdAndTourStatusIn(
                eq(attractionId),
                anyList()
        )).thenReturn(false);

        attractionService.delete(attractionId);

        verify(tourStopRepository).existsByAttractionIdAndTourStatusIn(
                eq(attractionId),
                argThat(statuses ->
                        statuses.size() == 2
                                && statuses.contains(com.iksanov.citytour.tour.entity.TourStatus.DRAFT)
                                && statuses.contains(com.iksanov.citytour.tour.entity.TourStatus.PUBLISHED)
                )
        );
    }
}