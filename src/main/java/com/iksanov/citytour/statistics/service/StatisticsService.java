package com.iksanov.citytour.statistics.service;

import com.iksanov.citytour.attraction.entity.AttractionCategory;
import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.statistics.dto.GuideStatisticsResponse;
import com.iksanov.citytour.statistics.dto.TopAttractionResponse;
import com.iksanov.citytour.statistics.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public List<GuideStatisticsResponse> getGuideStatistics(LocalDate from, LocalDate to) {
        validateDates(from, to);
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Object[]> tourRows =
                statisticsRepository.findPublishedToursForStatistics(
                        fromDateTime,
                        toDateTime
                );

        List<Object[]> categoryRows =
                statisticsRepository.findGuideCategories(
                        fromDateTime,
                        toDateTime
                );

        Map<Long, GuideData> guides = new HashMap<>();

        for (Object[] row : tourRows) {
            Long guideId = ((Number) row[1]).longValue();

            GuideData guide = guides.computeIfAbsent(
                    guideId,
                    id -> new GuideData(
                            id,
                            (String) row[2]
                    )
            );

            LocalDateTime startTime = (LocalDateTime) row[3];
            LocalDateTime endTime = (LocalDateTime) row[4];

            long durationSeconds = Duration.between(startTime, endTime).getSeconds();

            guide.totalSeconds += durationSeconds;
            guide.toursCount++;
            guide.maxSeats += ((Number) row[5]).longValue();
            guide.seatsSold += ((Number) row[6]).longValue();
            guide.totalRevenue = guide.totalRevenue.add((BigDecimal) row[7]);
        }

        Map<Long, Map<String, Long>> categoriesByGuide = new HashMap<>();

        for (Object[] row : categoryRows) {
            Long guideId = ((Number) row[0]).longValue();
            String category = (String) row[1];
            Long count = ((Number) row[2]).longValue();

            categoriesByGuide
                    .computeIfAbsent(
                            guideId,
                            id -> new HashMap<>()
                    )
                    .put(category, count);
        }

        List<GuideStatisticsResponse> result = new ArrayList<>();

        for (GuideData guide : guides.values()) {

            BigDecimal totalHours = BigDecimal
                    .valueOf(guide.totalSeconds)
                    .divide(
                            BigDecimal.valueOf(3600),
                            1,
                            RoundingMode.HALF_UP
                    );

            BigDecimal occupancyRate =
                    guide.maxSeats == 0
                            ? BigDecimal.ZERO.setScale(1)
                            : BigDecimal
                            .valueOf(guide.seatsSold)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    BigDecimal.valueOf(guide.maxSeats),
                                    1,
                                    RoundingMode.HALF_UP
                            );

            AttractionCategory topCategory =
                    findTopCategory(
                            categoriesByGuide.get(guide.guideId)
                    );

            result.add(
                    new GuideStatisticsResponse(
                            guide.guideId,
                            guide.guideName,
                            guide.toursCount,
                            totalHours,
                            guide.seatsSold,
                            guide.totalRevenue,
                            occupancyRate,
                            topCategory
                    )
            );
        }

        return result.stream()
                .sorted(
                        Comparator.comparing(
                                GuideStatisticsResponse::totalRevenue,
                                Comparator.reverseOrder()
                        )
                )
                .toList();
    }

    public List<TopAttractionResponse> getTopAttractions(LocalDate from, LocalDate to, int limit) {
        validateDates(from, to);

        if (limit < 1) {
            throw new BusinessException(
                    "Limit must be greater than 0",
                    "INVALID_LIMIT",
                    HttpStatus.BAD_REQUEST
            );
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Object[]> rows =
                statisticsRepository.findTopAttractions(
                        fromDateTime,
                        toDateTime,
                        PageRequest.of(0, limit)
                );

        return rows.stream()
                .map(row -> new TopAttractionResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        AttractionCategory.valueOf((String) row[2]),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();
    }

    private AttractionCategory findTopCategory(Map<String, Long> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        return categories.entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Long>comparingByValue(
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        Map.Entry::getKey
                                )
                )
                .map(entry ->
                        AttractionCategory.valueOf(entry.getKey())
                )
                .findFirst()
                .orElse(null);
    }

    private void validateDates(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException(
                    "Both from and to parameters are required",
                    "INVALID_DATE_RANGE",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (from.isAfter(to)) {
            throw new BusinessException(
                    "'from' must not be after 'to'",
                    "INVALID_DATE_RANGE",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private static class GuideData {
        private final Long guideId;
        private final String guideName;
        private long toursCount;
        private long totalSeconds;
        private long maxSeats;
        private long seatsSold;
        private BigDecimal totalRevenue = BigDecimal.ZERO;

        private GuideData(Long guideId, String guideName) {
            this.guideId = guideId;
            this.guideName = guideName;
        }
    }
}