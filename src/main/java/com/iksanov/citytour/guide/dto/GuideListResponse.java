package com.iksanov.citytour.guide.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuideListResponse {

    private List<GuideResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}