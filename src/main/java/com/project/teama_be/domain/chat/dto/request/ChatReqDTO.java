package com.project.teama_be.domain.chat.dto.request;

public class ChatReqDTO {

    public record CreateChatRoom(
            Long placeId
    ) {
    }

    public record UpdateChatRoomNotification(
            boolean notificationEnabled
    ) {
    }
}
