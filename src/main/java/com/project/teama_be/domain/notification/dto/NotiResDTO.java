package com.project.teama_be.domain.notification.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class NotiResDTO {

    @Builder
    public record SaveFcmToken(
            Long memberId,
            String fcmToken
    ) {
    }

    public record FcmMessage(String title, String body) {}

    public record NotificationSendResDTO(
            String title,
            String body,
            String fcmMessageId,  // FCM 서버가 응답한 메시지 ID
            Long notificationId   // 저장된 알림 ID
    ) {}

    public record ChatMessage(String title, String sender, String body){}

    public record NotificationListResDTO(
            Long id,
            String title,
            String body,
            boolean isRead,
            LocalDateTime sentAt
    ) {
    }

    public record NotificationPageResponseDTO(
            List<NotificationListResDTO> notifications,
            boolean hasNext,
            Long lastId
    ) {
    }
}
