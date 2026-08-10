package com.iksanov.citytour.statistics.service;

import com.iksanov.citytour.attraction.entity.AttractionCategory;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.statistics.dto.GuideStatisticsResponse;
import com.iksanov.citytour.statistics.dto.TopAttractionResponse;
import com.iksanov.citytour.statistics.repository.StatisticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void shouldReturnGuideStatistics_whenDataExists() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        List<Object[]> tourRows = List.<Object[]>of(
                new Object[]{
                        1L,
                        10L,
                        "Rustam Tursunov",
                        LocalDateTime.of(2026, 8, 5, 10, 0),
                        LocalDateTime.of(2026, 8, 5, 12, 0),
                        10L,
                        8L,
                        new BigDecimal("500000")
                }
        );

        List<Object[]> categoryRows = List.<Object[]>of(
                new Object[]{
                        10L,
                        "MONUMENT",
                        3L
                },
                new Object[]{
                        10L,
                        "MUSEUM",
                        1L
                }
        );

        when(statisticsRepository.findPublishedToursForStatistics(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay()
        )).thenReturn(tourRows);

        when(statisticsRepository.findGuideCategories(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay()
        )).thenReturn(categoryRows);

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(1, result.size());

        GuideStatisticsResponse statistics = result.get(0);

        assertEquals(10L, statistics.guideId());
        assertEquals("Rustam Tursunov", statistics.guideName());
        assertEquals(1L, statistics.toursCount());
        assertEquals(new BigDecimal("2.0"), statistics.totalHours());
        assertEquals(8L, statistics.seatsSold());
        assertEquals(new BigDecimal("500000"), statistics.totalRevenue());
        assertEquals(new BigDecimal("80.0"), statistics.occupancyRate());
        assertEquals(
                AttractionCategory.MONUMENT,
                statistics.topCategory()
        );
    }

    @Test
    void shouldAggregateMultipleTours_forSameGuide() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        List<Object[]> tourRows = List.<Object[]>of(
                new Object[]{
                        1L,
                        10L,
                        "Rustam Tursunov",
                        LocalDateTime.of(2026, 8, 1, 10, 0),
                        LocalDateTime.of(2026, 8, 1, 12, 0),
                        10L,
                        8L,
                        new BigDecimal("500000")
                },
                new Object[]{
                        2L,
                        10L,
                        "Rustam Tursunov",
                        LocalDateTime.of(2026, 8, 2, 14, 0),
                        LocalDateTime.of(2026, 8, 2, 17, 0),
                        20L,
                        15L,
                        new BigDecimal("700000")
                }
        );

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(tourRows);

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                10L,
                                "MUSEUM",
                                5L
                        }
                )
        );

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(1, result.size());

        GuideStatisticsResponse statistics = result.get(0);

        assertEquals(2L, statistics.toursCount());
        assertEquals(new BigDecimal("5.0"), statistics.totalHours());
        assertEquals(23L, statistics.seatsSold());
        assertEquals(
                new BigDecimal("1200000"),
                statistics.totalRevenue()
        );
        assertEquals(
                new BigDecimal("76.7"),
                statistics.occupancyRate()
        );
    }

    @Test
    void shouldCalculateZeroOccupancy_whenMaxSeatsIsZero() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 1);

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                1L,
                                10L,
                                "Guide",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 0),
                                0L,
                                0L,
                                new BigDecimal("100000")
                        }
                )
        );

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(List.of());

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(
                new BigDecimal("0.0"),
                result.get(0).occupancyRate()
        );
    }

    @Test
    void shouldRoundTotalHours_toOneDecimal() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 1);

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                1L,
                                10L,
                                "Guide",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 1),
                                10L,
                                5L,
                                new BigDecimal("100000")
                        }
                )
        );

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(List.of());

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(
                new BigDecimal("1.0"),
                result.get(0).totalHours()
        );
    }

    @Test
    void shouldSelectTopCategory_byCount() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 1);

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                1L,
                                10L,
                                "Guide",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 0),
                                10L,
                                5L,
                                new BigDecimal("100000")
                        }
                )
        );

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{10L, "MUSEUM", 2L},
                        new Object[]{10L, "MONUMENT", 5L},
                        new Object[]{10L, "PARK", 1L}
                )
        );

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(
                AttractionCategory.MONUMENT,
                result.get(0).topCategory()
        );
    }

    @Test
    void shouldSelectAlphabeticallyFirstCategory_whenCountsAreEqual() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 1);

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                1L,
                                10L,
                                "Guide",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 0),
                                10L,
                                5L,
                                new BigDecimal("100000")
                        }
                )
        );

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{10L, "PARK", 5L},
                        new Object[]{10L, "MUSEUM", 5L}
                )
        );

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(
                AttractionCategory.MUSEUM,
                result.get(0).topCategory()
        );
    }

    @Test
    void shouldReturnNullTopCategory_whenGuideHasNoCategories() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 1);

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                1L,
                                10L,
                                "Guide",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 0),
                                10L,
                                5L,
                                new BigDecimal("100000")
                        }
                )
        );

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(List.of());

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertNull(result.get(0).topCategory());
    }

    @Test
    void shouldSortGuides_byRevenueDescending() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 1);

        when(statisticsRepository.findPublishedToursForStatistics(
                any(),
                any()
        )).thenReturn(
                List.<Object[]>of(
                        new Object[]{
                                1L,
                                10L,
                                "Guide A",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 0),
                                10L,
                                5L,
                                new BigDecimal("100000")
                        },
                        new Object[]{
                                2L,
                                20L,
                                "Guide B",
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 11, 0),
                                10L,
                                5L,
                                new BigDecimal("500000")
                        }
                )
        );

        when(statisticsRepository.findGuideCategories(
                any(),
                any()
        )).thenReturn(List.of());

        List<GuideStatisticsResponse> result =
                statisticsService.getGuideStatistics(from, to);

        assertEquals(2, result.size());
        assertEquals(20L, result.get(0).guideId());
        assertEquals(10L, result.get(1).guideId());
    }

    @Test
    void shouldThrow_whenFromIsNull() {
        LocalDate to = LocalDate.of(2026, 8, 10);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getGuideStatistics(null, to)
        );

        assertEquals("INVALID_DATE_RANGE", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldThrow_whenToIsNull() {
        LocalDate from = LocalDate.of(2026, 8, 1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getGuideStatistics(from, null)
        );

        assertEquals("INVALID_DATE_RANGE", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldThrow_whenFromIsAfterTo() {
        LocalDate from = LocalDate.of(2026, 8, 10);
        LocalDate to = LocalDate.of(2026, 8, 1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getGuideStatistics(from, to)
        );

        assertEquals("INVALID_DATE_RANGE", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldReturnTopAttractions_whenDataExists() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        List<Object[]> rows = List.<Object[]>of(
                new Object[]{
                        1L,
                        "Registan Square",
                        "MONUMENT",
                        10L,
                        500L
                },
                new Object[]{
                        2L,
                        "Amir Timur Museum",
                        "MUSEUM",
                        7L,
                        300L
                }
        );

        when(statisticsRepository.findTopAttractions(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PageRequest.of(0, 5)
        )).thenReturn(rows);

        List<TopAttractionResponse> result =
                statisticsService.getTopAttractions(
                        from,
                        to,
                        5
                );

        assertEquals(2, result.size());

        TopAttractionResponse first = result.get(0);

        assertEquals(1L, first.attractionId());
        assertEquals("Registan Square", first.name());
        assertEquals(
                AttractionCategory.MONUMENT,
                first.category()
        );
        assertEquals(10L, first.tourCount());
        assertEquals(500L, first.visitorCount());
    }

    @Test
    void shouldPassLimitToRepository() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        when(statisticsRepository.findTopAttractions(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PageRequest.of(0, 3)
        )).thenReturn(List.of());

        List<TopAttractionResponse> result =
                statisticsService.getTopAttractions(
                        from,
                        to,
                        3
                );

        assertEquals(0, result.size());
    }

    @Test
    void shouldThrow_whenLimitIsZero() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getTopAttractions(
                        from,
                        to,
                        0
                )
        );

        assertEquals("INVALID_LIMIT", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldThrow_whenLimitIsNegative() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 10);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getTopAttractions(
                        from,
                        to,
                        -1
                )
        );

        assertEquals("INVALID_LIMIT", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldThrow_whenTopAttractionsFromIsAfterTo() {
        LocalDate from = LocalDate.of(2026, 8, 10);
        LocalDate to = LocalDate.of(2026, 8, 1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getTopAttractions(
                        from,
                        to,
                        5
                )
        );

        assertEquals("INVALID_DATE_RANGE", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldThrow_whenTopAttractionsFromIsNull() {
        LocalDate to = LocalDate.of(2026, 8, 10);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getTopAttractions(
                        null,
                        to,
                        5
                )
        );

        assertEquals("INVALID_DATE_RANGE", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }

    @Test
    void shouldThrow_whenTopAttractionsToIsNull() {
        LocalDate from = LocalDate.of(2026, 8, 1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> statisticsService.getTopAttractions(
                        from,
                        null,
                        5
                )
        );

        assertEquals("INVALID_DATE_RANGE", exception.getCode());

        verifyNoInteractions(statisticsRepository);
    }
}