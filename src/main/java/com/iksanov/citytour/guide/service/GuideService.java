package com.iksanov.citytour.guide.service;

import com.iksanov.citytour.guide.dto.GuideCreateRequest;
import com.iksanov.citytour.guide.dto.GuideListResponse;
import com.iksanov.citytour.guide.dto.GuideResponse;
import com.iksanov.citytour.guide.dto.GuideUpdateRequest;
import com.iksanov.citytour.guide.entity.Language;
import com.iksanov.citytour.guide.mapper.GuideMapper;
import com.iksanov.citytour.guide.repository.GuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuideService {

    private final GuideRepository guideRepository;
    private final GuideMapper guideMapper;

    public GuideResponse getById(Long id) {
        return null;
    }

    public GuideListResponse getAll(
            Boolean active,
            Language language,
            int page,
            int size
    ) {
        return null;
    }

    public GuideResponse create(GuideCreateRequest request) {
        return null;
    }

    public GuideResponse update(Long id, GuideUpdateRequest request) {
        return null;
    }

    public void delete(Long id) {
    }
}