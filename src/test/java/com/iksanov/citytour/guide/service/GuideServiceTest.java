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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private GuideMapper guideMapper;

    @Mock
    private TourRepository tourRepository;

    private GuideService guideService;

    @BeforeEach
    void setUp() {
        guideService = new GuideService(
                guideRepository,
                guideMapper,
                tourRepository
        );
    }

    @Test
    void getById_shouldReturnGuide_whenGuideExists() {
        // given
        Long guideId = 1L;

        Guide guide = new Guide();
        guide.setId(guideId);
        guide.setFullName("John Doe");

        GuideResponse expectedResponse = GuideResponse.builder()
                .id(guideId)
                .fullName("John Doe")
                .build();

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toResponse(guide))
                .thenReturn(expectedResponse);

        // when
        GuideResponse actualResponse = guideService.getById(guideId);

        // then
        assertSame(expectedResponse, actualResponse);

        verify(guideRepository).findById(guideId);
        verify(guideMapper).toResponse(guide);
        verifyNoInteractions(tourRepository);
    }

    @Test
    void getById_shouldThrowGuideNotFoundException_whenGuideDoesNotExist() {
        // given
        Long guideId = 999L;

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.getById(guideId)
        );

        verify(guideRepository).findById(guideId);
        verifyNoInteractions(guideMapper);
        verifyNoInteractions(tourRepository);
    }

    @Test
    void getAll_shouldReturnPagedGuides_withFiltersAndPagination() {
        // given
        Boolean active = true;
        Language language = Language.EN;
        int page = 1;
        int size = 10;

        Guide guide1 = new Guide();
        guide1.setId(1L);

        Guide guide2 = new Guide();
        guide2.setId(2L);

        List<Guide> guides = List.of(guide1, guide2);

        Page<Guide> guidePage = new PageImpl<>(
                guides,
                PageRequest.of(page, size),
                12
        );

        GuideResponse response1 = GuideResponse.builder()
                .id(1L)
                .build();

        GuideResponse response2 = GuideResponse.builder()
                .id(2L)
                .build();

        GuideListResponse expectedResponse = GuideListResponse.builder()
                .content(List.of(response1, response2))
                .page(page)
                .size(size)
                .totalElements(12)
                .totalPages(2)
                .build();

        when(guideRepository.findAllByFilters(
                eq(active),
                eq(language),
                any(Pageable.class)
        )).thenReturn(guidePage);

        when(guideMapper.toResponseList(guides))
                .thenReturn(List.of(response1, response2));

        // when
        GuideListResponse actualResponse =
                guideService.getAll(active, language, page, size);

        // then
        assertSame(expectedResponse.getContent(), actualResponse.getContent());
        assertEquals(page, actualResponse.getPage());
        assertEquals(size, actualResponse.getSize());
        assertEquals(12, actualResponse.getTotalElements());
        assertEquals(2, actualResponse.getTotalPages());

        verify(guideRepository).findAllByFilters(
                eq(active),
                eq(language),
                eq(PageRequest.of(page, size))
        );

        verify(guideMapper).toResponseList(guides);
        verifyNoInteractions(tourRepository);
    }

    @Test
    void getAll_shouldPassNullFilters_whenFiltersAreNotSpecified() {
        // given
        int page = 0;
        int size = 20;

        Page<Guide> guidePage = new PageImpl<>(
                List.of(),
                PageRequest.of(page, size),
                0
        );

        when(guideRepository.findAllByFilters(
                eq(null),
                eq(null),
                eq(PageRequest.of(page, size))
        )).thenReturn(guidePage);

        when(guideMapper.toResponseList(List.of()))
                .thenReturn(List.of());

        // when
        GuideListResponse actualResponse =
                guideService.getAll(null, null, page, size);

        // then
        assertNotNull(actualResponse);
        assertEquals(0, actualResponse.getPage());
        assertEquals(20, actualResponse.getSize());
        assertEquals(0, actualResponse.getTotalElements());
        assertEquals(0, actualResponse.getTotalPages());

        verify(guideRepository).findAllByFilters(
                null,
                null,
                PageRequest.of(page, size)
        );

        verify(guideMapper).toResponseList(List.of());
    }

    @Test
    void create_shouldSaveGuideWithAllRequestedLanguages() {
        // given
        GuideCreateRequest request = new GuideCreateRequest();
        request.setFullName("John Doe");
        request.setPhone("+998901234567");
        request.setLanguages(Set.of(
                Language.EN,
                Language.RU
        ));
        request.setExperienceYears(5);

        Guide mappedGuide = new Guide();
        mappedGuide.setFullName("John Doe");
        mappedGuide.setPhone("+998901234567");
        mappedGuide.setExperienceYears(5);

        GuideResponse expectedResponse = GuideResponse.builder()
                .id(1L)
                .fullName("John Doe")
                .build();

        when(guideMapper.toEntity(request))
                .thenReturn(mappedGuide);

        when(guideRepository.save(any(Guide.class)))
                .thenAnswer(invocation -> {
                    Guide guide = invocation.getArgument(0);
                    guide.setId(1L);
                    return guide;
                });

        when(guideMapper.toResponse(mappedGuide))
                .thenReturn(expectedResponse);

        // when
        GuideResponse actualResponse = guideService.create(request);

        // then
        assertSame(expectedResponse, actualResponse);

        assertNotNull(mappedGuide.getLanguages());
        assertEquals(2, mappedGuide.getLanguages().size());

        Set<Language> actualLanguages = mappedGuide.getLanguages()
                .stream()
                .map(language -> language.getId().getLanguage())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                Set.of(Language.EN, Language.RU),
                actualLanguages
        );

        verify(guideMapper).toEntity(request);
        verify(guideRepository).save(mappedGuide);
        verify(guideMapper).toResponse(mappedGuide);
        verifyNoInteractions(tourRepository);
    }

    @Test
    void create_shouldCreateSeparateGuideLanguageEntityForEachLanguage() {
        // given
        GuideCreateRequest request = new GuideCreateRequest();
        request.setFullName("John Doe");
        request.setPhone("+998901234567");
        request.setLanguages(Set.of(Language.EN, Language.RU, Language.UZ));
        request.setExperienceYears(3);

        Guide guide = new Guide();

        when(guideMapper.toEntity(request))
                .thenReturn(guide);

        when(guideRepository.save(any(Guide.class)))
                .thenReturn(guide);

        when(guideMapper.toResponse(guide))
                .thenReturn(GuideResponse.builder().build());

        // when
        guideService.create(request);

        // then
        assertNotNull(guide.getLanguages());
        assertEquals(3, guide.getLanguages().size());

        assertTrue(
                guide.getLanguages()
                        .stream()
                        .allMatch(language -> language.getGuide() == guide)
        );

        verify(guideRepository).save(guide);
    }

    @Test
    void update_shouldUpdateGuideAndReplaceLanguages() {
        // given
        Long guideId = 1L;

        Guide guide = new Guide();
        guide.setId(guideId);
        guide.setFullName("Old Name");

        GuideLanguage oldLanguage = new GuideLanguage();
        oldLanguage.setGuide(guide);
        oldLanguage.setId(
                new GuideLanguageId(guideId, Language.EN)
        );

        guide.setLanguages(new HashSet<>(Set.of(oldLanguage)));

        GuideUpdateRequest request = new GuideUpdateRequest();
        request.setFullName("New Name");
        request.setPhone("+998901234567");
        request.setLanguages(Set.of(
                Language.RU,
                Language.UZ
        ));
        request.setExperienceYears(7);
        request.setActive(true);

        GuideResponse expectedResponse = GuideResponse.builder()
                .id(guideId)
                .fullName("New Name")
                .build();

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        doAnswer(invocation -> {
            GuideUpdateRequest updateRequest = invocation.getArgument(0);
            Guide target = invocation.getArgument(1);

            target.setFullName(updateRequest.getFullName());
            target.setPhone(updateRequest.getPhone());
            target.setExperienceYears(updateRequest.getExperienceYears());
            target.setActive(updateRequest.getActive());

            return null;
        }).when(guideMapper).updateEntity(request, guide);

        when(guideMapper.toResponse(guide))
                .thenReturn(expectedResponse);

        // when
        GuideResponse actualResponse =
                guideService.update(guideId, request);

        // then
        assertSame(expectedResponse, actualResponse);

        assertEquals("New Name", guide.getFullName());
        assertEquals("+998901234567", guide.getPhone());
        assertEquals(7, guide.getExperienceYears());
        assertTrue(guide.getActive());

        assertNotNull(guide.getLanguages());
        assertEquals(2, guide.getLanguages().size());

        Set<Language> actualLanguages = guide.getLanguages()
                .stream()
                .map(language -> language.getId().getLanguage())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                Set.of(Language.RU, Language.UZ),
                actualLanguages
        );

        verify(guideRepository).findById(guideId);
        verify(guideMapper).updateEntity(request, guide);
        verify(guideMapper).toResponse(guide);

        verifyNoInteractions(tourRepository);
    }

    @Test
    void update_shouldThrowGuideNotFoundException_whenGuideDoesNotExist() {
        // given
        Long guideId = 999L;

        GuideUpdateRequest request = new GuideUpdateRequest();

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.update(guideId, request)
        );

        verify(guideRepository).findById(guideId);
        verifyNoInteractions(guideMapper);
        verifyNoInteractions(tourRepository);
    }

    @Test
    void delete_shouldDeleteGuide_whenGuideHasNoActiveTours() {
        // given
        Long guideId = 1L;

        Guide guide = new Guide();
        guide.setId(guideId);

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        )).thenReturn(false);

        // when
        guideService.delete(guideId);

        // then
        verify(guideRepository).findById(guideId);

        verify(tourRepository).existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        );

        verify(guideRepository).delete(guide);
    }

    @Test
    void delete_shouldThrowBusinessException_whenGuideHasActiveTours() {
        // given
        Long guideId = 1L;

        Guide guide = new Guide();
        guide.setId(guideId);

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        )).thenReturn(true);

        // when & then
        assertThrows(
                BusinessException.class,
                () -> guideService.delete(guideId)
        );

        verify(guideRepository).findById(guideId);

        verify(tourRepository).existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        );

        verify(guideRepository, never()).delete(any());
    }

    @Test
    void delete_shouldDeleteGuide_whenGuideHasOnlyCancelledTours() {
        // given
        Long guideId = 1L;

        Guide guide = new Guide();
        guide.setId(guideId);

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.of(guide));

        when(tourRepository.existsByGuideIdAndStatusIn(
                guideId,
                List.of(
                        TourStatus.DRAFT,
                        TourStatus.PUBLISHED
                )
        )).thenReturn(false);

        // when
        guideService.delete(guideId);

        // then
        verify(guideRepository).delete(guide);
    }

    @Test
    void delete_shouldThrowGuideNotFoundException_whenGuideDoesNotExist() {
        // given
        Long guideId = 999L;

        when(guideRepository.findById(guideId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.delete(guideId)
        );

        verify(guideRepository).findById(guideId);

        verifyNoInteractions(tourRepository);
        verify(guideRepository, never()).delete(any());
    }
}