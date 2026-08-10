package com.iksanov.citytour.guide.service;

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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private GuideMapper guideMapper;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private GuideService guideService;

    @Test
    void shouldReturnGuide_whenGuideExists() {
        Long guideId = 1L;

        Guide guide = createGuide(guideId);

        GuideResponse response = new GuideResponse(
                guideId,
                "Rustam Tursunov",
                "+998903333333",
                Set.of(Language.UZ, Language.EN),
                12,
                true
        );

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toResponse(guide))
                .thenReturn(response);

        GuideResponse result = guideService.getById(guideId);

        assertSame(response, result);

        verify(guideRepository).findById(guideId);
        verify(guideMapper).toResponse(guide);
    }

    @Test
    void shouldThrow_whenGuideDoesNotExist() {
        Long guideId = 999L;

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.getById(guideId)
        );

        verify(guideMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnGuides_whenFiltersApplied() {
        Boolean active = true;
        Language language = Language.EN;
        int page = 1;
        int size = 2;

        Guide guide1 = createGuide(1L);
        Guide guide2 = createGuide(2L);

        List<Guide> guides = List.of(guide1, guide2);

        Page<Guide> guidePage = new PageImpl<>(
                guides,
                PageRequest.of(page, size),
                5
        );

        List<GuideResponse> responses = List.of(
                new GuideResponse(
                        1L,
                        "Guide 1",
                        "+998901111111",
                        Set.of(Language.EN),
                        5,
                        true
                ),
                new GuideResponse(
                        2L,
                        "Guide 2",
                        "+998902222222",
                        Set.of(Language.EN, Language.UZ),
                        7,
                        true
                )
        );

        when(guideRepository.findAllByFilters(
                eq(active),
                eq(language),
                any(Pageable.class)
        )).thenReturn(guidePage);

        when(guideMapper.toResponseList(guides))
                .thenReturn(responses);

        GuideListResponse result =
                guideService.getAll(active, language, page, size);

        assertEquals(responses, result.content());
        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(guideRepository).findAllByFilters(
                eq(active),
                eq(language),
                eq(PageRequest.of(page, size))
        );

        verify(guideMapper).toResponseList(guides);
    }

    @Test
    void shouldCreateGuide_withLanguages() {
        Set<Language> languages = Set.of(
                Language.UZ,
                Language.EN
        );

        GuideCreateRequest request = new GuideCreateRequest(
                "Rustam Tursunov",
                "+998903333333",
                languages,
                12
        );

        Guide guide = createGuide(null);

        Guide savedGuide = createGuide(1L);

        GuideResponse response = new GuideResponse(
                1L,
                "Rustam Tursunov",
                "+998903333333",
                languages,
                12,
                true
        );

        when(guideMapper.toEntity(request))
                .thenReturn(guide);

        when(guideRepository.save(any(Guide.class)))
                .thenReturn(savedGuide);

        when(guideMapper.toResponse(savedGuide))
                .thenReturn(response);

        GuideResponse result =
                guideService.create(request);

        assertSame(response, result);

        ArgumentCaptor<Guide> guideCaptor =
                ArgumentCaptor.forClass(Guide.class);

        verify(guideRepository).save(guideCaptor.capture());

        Guide savedEntity = guideCaptor.getValue();

        assertEquals(
                languages.size(),
                savedEntity.getLanguages().size()
        );

        assertEquals(
                languages,
                savedEntity.getLanguages()
                        .stream()
                        .map(GuideLanguage::getId)
                        .map(GuideLanguageId::getLanguage)
                        .collect(java.util.stream.Collectors.toSet())
        );

        for (GuideLanguage guideLanguage :
                savedEntity.getLanguages()) {

            assertSame(
                    savedEntity,
                    guideLanguage.getGuide()
            );

            assertEquals(
                    guideLanguage.getId().getLanguage(),
                    guideLanguage.getId().getLanguage()
            );

            assertEquals(
                    null,
                    guideLanguage.getId().getGuideId()
            );
        }

        verify(guideMapper).toEntity(request);
        verify(guideMapper).toResponse(savedGuide);
    }

    @Test
    void shouldGetById_whenGuideDoesNotExist() {
        Long guideId = 999L;

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.update(
                        guideId,
                        new GuideUpdateRequest(
                                "Updated Guide",
                                "+998901111111",
                                Set.of(Language.EN),
                                5,
                                true
                        )
                )
        );

        verify(guideMapper, never())
                .updateEntity(any(), any());

        verify(entityManager, never())
                .flush();
    }

    @Test
    void shouldUpdateGuide_andReplaceLanguages() {
        Long guideId = 1L;

        Guide guide = createGuide(guideId);

        GuideLanguage oldLanguage =
                createGuideLanguage(
                        guide,
                        guideId,
                        Language.RU
                );

        guide.setLanguages(
                new HashSet<>(Set.of(oldLanguage))
        );

        Set<Language> newLanguages = Set.of(
                Language.EN,
                Language.UZ
        );

        GuideUpdateRequest request =
                new GuideUpdateRequest(
                        "Updated Guide",
                        "+998901111111",
                        newLanguages,
                        8,
                        true
                );

        GuideResponse response =
                new GuideResponse(
                        guideId,
                        "Updated Guide",
                        "+998901111111",
                        newLanguages,
                        8,
                        true
                );

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toResponse(guide))
                .thenReturn(response);

        GuideResponse result =
                guideService.update(guideId, request);

        assertSame(response, result);

        verify(guideMapper)
                .updateEntity(request, guide);

        verify(entityManager)
                .flush();

        assertEquals(
                newLanguages,
                guide.getLanguages()
                        .stream()
                        .map(GuideLanguage::getId)
                        .map(GuideLanguageId::getLanguage)
                        .collect(java.util.stream.Collectors.toSet())
        );

        assertEquals(
                2,
                guide.getLanguages().size()
        );

        for (GuideLanguage guideLanguage :
                guide.getLanguages()) {

            assertSame(
                    guide,
                    guideLanguage.getGuide()
            );

            assertEquals(
                    guideId,
                    guideLanguage.getId().getGuideId()
            );
        }

        verify(guideMapper).toResponse(guide);
    }

    @Test
    void shouldDeleteGuide_whenGuideHasNoActiveTours() {
        Long guideId = 1L;

        Guide guide = createGuide(guideId);

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        )).thenReturn(false);

        guideService.delete(guideId);

        verify(tourRepository)
                .existsByGuideIdAndStatusIn(
                        guideId,
                        List.of(
                                TourStatus.DRAFT,
                                TourStatus.PUBLISHED
                        )
                );

        verify(guideRepository)
                .delete(guide);
    }

    @Test
    void shouldThrow_whenDeletingGuideWithActiveTours() {
        Long guideId = 1L;

        Guide guide = createGuide(guideId);

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        )).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> guideService.delete(guideId)
                );

        assertEquals(
                "GUIDE_HAS_ACTIVE_TOURS",
                exception.getCode()
        );

        verify(guideRepository, never())
                .delete(any());
    }

    @Test
    void shouldThrow_whenDeletingNonExistingGuide() {
        Long guideId = 999L;

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.delete(guideId)
        );

        verify(tourRepository, never())
                .existsByGuideIdAndStatusIn(anyLong(), anyList());

        verify(guideRepository, never())
                .delete(any());
    }

    private Guide createGuide(Long id) {
        Guide guide = new Guide();

        guide.setId(id);
        guide.setFullName("Rustam Tursunov");
        guide.setPhone("+998903333333");
        guide.setExperienceYears(12);
        guide.setActive(true);
        guide.setLanguages(new HashSet<>());

        return guide;
    }

    private GuideLanguage createGuideLanguage(
            Guide guide,
            Long guideId,
            Language language
    ) {
        GuideLanguage guideLanguage = new GuideLanguage();

        guideLanguage.setGuide(guide);
        guideLanguage.setId(
                new GuideLanguageId(
                        guideId,
                        language
                )
        );

        return guideLanguage;
    }
}