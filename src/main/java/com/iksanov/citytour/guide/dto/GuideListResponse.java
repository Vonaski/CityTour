package com.iksanov.citytour.guide.dto;

import java.util.List;

public record GuideListResponse(
        List<GuideResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}