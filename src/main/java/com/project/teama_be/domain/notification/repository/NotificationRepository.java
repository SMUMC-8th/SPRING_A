package com.project.teama_be.domain.notification.repository;

import com.project.teama_be.domain.notification.entity.Noti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Noti, Long> {
    // 읽지 않은 알림 수 조회용 (receiverId = memberId)
    long countByMemberIdAndIsReadFalse(Long memberId);
}

