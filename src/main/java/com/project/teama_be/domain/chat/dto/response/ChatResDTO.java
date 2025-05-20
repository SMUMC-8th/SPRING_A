package com.project.teama_be.domain.chat.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResDTO {

    @Builder
    public record ChatRoom(
            Long chatRoomId,
            String chatRoomName,
            String placeName,
            int participantCount,
            LocalDateTime createdAt
    ) {
    }

    // 개별 채팅방 정보
    @Builder
    public record ChatRoomItem(
            Long chatRoomId,
            String chatRoomName,
            String locationName,
            int participantCount,
            String lastMessageAt,
            String representativePostImageUrl
    ) {
    }

    // 채팅방 목록 조회 결과
    @Builder
    public record ChatRoomList(
            List<ChatRoomItem> RegionChatRoomListDTO,
            Long nextCursor,
            boolean hasNext
    ) {
    }

    // 채팅방 알림 설정 정보
    @Builder
    public record ChatRoomNotificationInfo(
            Long chatRoomId,
            boolean notificationEnabled,
            String updatedAt
    ) {
    }

    @Builder
    public record SendBirdTokenInfo(
            String sendBirdUserId,
            String sendBirdToken,
            long expiredAt
    ) {
    }
}
