package com.project.teama_be.domain.notification.repository;

import com.project.teama_be.domain.notification.entity.Noti;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotiRepository extends JpaRepository<Noti, Long> {
    // 읽지 않은 알림 수 조회용 (receiverId = memberId)
    long countByMemberIdAndIsReadFalse(Long memberId);

    @Query("""
    SELECT n FROM Noti n
    WHERE n.member.id = :memberId
      AND (:cursor IS NULL OR n.id < :cursor)
    ORDER BY n.id DESC
""")
    List<Noti> findByMemberIdWithCursor(@Param("memberId")Long memberId,
                                        @Param("cursor")Long cursor, Pageable pageable);
}

