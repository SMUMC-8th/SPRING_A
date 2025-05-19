package com.project.teama_be.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public class ChatReqDTO {

    public record CreateChatRoom(
            @NotNull(message = "위치 ID는 필수 입력값입니다.")
            Long placeId
    ) {
    }

    public record UpdateChatRoomNotification(
            boolean notificationEnabled
    ) {
    }
}
