package com.project.teama_be.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NotiResDTO {

    public record FcmMessage(String title, String body) {}

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
