package com.iksanov.citytour.guide.repository;

import com.iksanov.citytour.guide.entity.Guide;
import com.iksanov.citytour.guide.entity.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    @Query("""
        SELECT DISTINCT g
        FROM Guide g
        LEFT JOIN g.languages l
        WHERE (:active IS NULL OR g.active = :active)
          AND (:language IS NULL OR l.id.language = :language)
        """)
    Page<Guide> findAllByFilters(
            @Param("active") Boolean active,
            @Param("language") Language language,
            Pageable pageable
    );
}