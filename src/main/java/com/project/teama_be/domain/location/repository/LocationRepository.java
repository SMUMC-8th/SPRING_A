package com.project.teama_be.domain.location.repository;

import com.project.teama_be.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByPlaceName(String s);

    Location findByPlaceName(String s);

    List<Location> findByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat,
            BigDecimal maxLat,
            BigDecimal minLng,
            BigDecimal maxLng
    );
}
