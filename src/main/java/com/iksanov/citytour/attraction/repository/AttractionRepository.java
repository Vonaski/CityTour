package com.iksanov.citytour.attraction.repository;

import com.iksanov.citytour.attraction.entity.Attraction;
import com.iksanov.citytour.attraction.entity.AttractionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    @Query("""
            SELECT a
            FROM Attraction a
            WHERE (:category IS NULL OR a.category = :category)
              AND (
                    :search IS NULL
                    OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Attraction> findAllByFilters(
            @Param("category") AttractionCategory category,
            @Param("search") String search,
            Pageable pageable
    );
}