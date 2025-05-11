package com.project.teama_be.domain.location.repository;

import com.project.teama_be.domain.location.entity.Location;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByPlaceName(String s);

    Location findByPlaceName(String s);
}
