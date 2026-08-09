package com.iksanov.citytour.guide.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.iksanov.citytour.common.exception.BusinessException;
import com.iksanov.citytour.common.exception.GuideNotFoundException;
import com.iksanov.citytour.guide.dto.GuideCreateRequest;
import com.iksanov.citytour.guide.dto.GuideListResponse;
import com.iksanov.citytour.guide.dto.GuideResponse;
import com.iksanov.citytour.guide.dto.GuideUpdateRequest;
import com.iksanov.citytour.guide.entity.Guide;
import com.iksanov.citytour.guide.entity.GuideLanguage;
import com.iksanov.citytour.guide.entity.GuideLanguageId;
import com.iksanov.citytour.guide.entity.Language;
import com.iksanov.citytour.guide.mapper.GuideMapper;
import com.iksanov.citytour.guide.repository.GuideRepository;
import com.iksanov.citytour.tour.entity.TourStatus;
import com.iksanov.citytour.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuideService {

    private final GuideRepository guideRepository;
    private final GuideMapper guideMapper;
    private final TourRepository tourRepository;

    public GuideResponse getById(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new GuideNotFoundException(id));

        return guideMapper.toResponse(guide);
    }

    public GuideListResponse getAll(Boolean active, Language language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Guide> guidePage = guideRepository.findAllByFilters(active, language, pageable);

        return GuideListResponse.builder()
                .content(guideMapper.toResponseList(guidePage.getContent()))
                .page(guidePage.getNumber())
                .size(guidePage.getSize())
                .totalElements(guidePage.getTotalElements())
                .totalPages(guidePage.getTotalPages())
                .build();
    }

    @Transactional
    public GuideResponse create(GuideCreateRequest request) {
        Guide guide = guideMapper.toEntity(request);
        Set<GuideLanguage> languages = new HashSet<>();

        for (Language language : request.getLanguages()) {
            GuideLanguage guideLanguage = new GuideLanguage();
            guideLanguage.setGuide(guide);
            guideLanguage.setId(new GuideLanguageId(null, language));
            languages.add(guideLanguage);
        }

        guide.setLanguages(languages);
        Guide savedGuide = guideRepository.save(guide);
        return guideMapper.toResponse(savedGuide);
    }

    @Transactional
    public GuideResponse update(Long id, GuideUpdateRequest request) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new GuideNotFoundException(id));

        guideMapper.updateEntity(request, guide);
        guide.getLanguages().clear();

        for (Language language : request.getLanguages()) {
            GuideLanguage guideLanguage = new GuideLanguage();
            guideLanguage.setGuide(guide);
            guideLanguage.setId(new GuideLanguageId(guide.getId(), language));
            guide.getLanguages().add(guideLanguage);
        }

        return guideMapper.toResponse(guide);
    }

    @Transactional
    public void delete(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new GuideNotFoundException(id));

        boolean hasActiveTours = tourRepository.existsByGuideIdAndStatusIn(id,
                List.of(TourStatus.DRAFT, TourStatus.PUBLISHED)
        );

        if (hasActiveTours) {
            throw new BusinessException("Cannot delete guide with active tours",
                                        "GUIDE_HAS_ACTIVE_TOURS",
                                         HttpStatus.CONFLICT);
        }
        guideRepository.delete(guide);
    }
}