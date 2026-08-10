package com.iksanov.citytour.statistics.controller;

import com.iksanov.citytour.statistics.dto.GuideStatisticsResponse;
import com.iksanov.citytour.statistics.dto.TopAttractionResponse;
import com.iksanov.citytour.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/guides")
    public ResponseEntity<List<GuideStatisticsResponse>> getGuideStatistics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return ResponseEntity.ok(
                statisticsService.getGuideStatistics(from, to)
        );
    }

    @GetMapping("/attractions/top")
    public ResponseEntity<List<TopAttractionResponse>> getTopAttractions(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(defaultValue = "10")
            int limit
    ) {
        return ResponseEntity.ok(
                statisticsService.getTopAttractions(
                        from,
                        to,
                        limit
                )
        );
    }
}