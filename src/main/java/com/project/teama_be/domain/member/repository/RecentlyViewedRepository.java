package com.project.teama_be.domain.member.repository;

import com.project.teama_be.domain.member.entity.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    RecentlyViewed findByMemberIdAndPostId(Long memberId, Long postId);

    RecentlyViewed findByPostId(Long postId);
}
