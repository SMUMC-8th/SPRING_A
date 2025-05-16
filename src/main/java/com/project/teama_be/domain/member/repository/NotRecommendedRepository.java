package com.project.teama_be.domain.member.repository;

import com.project.teama_be.domain.member.entity.NotRecommended;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotRecommendedRepository extends JpaRepository<NotRecommended, Long> {
}
