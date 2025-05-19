package com.project.teama_be.domain.chat.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResDTO {

    @Builder
    public record ChatRoom(
            String chatRoomId,
            String chatRoomName,
            String placeName,
            int participantCount,
            LocalDateTime createdAt
    ) {
    }

    // 지역별 미참여 채팅방 목록의 개별 채팅방 정보
    @Builder
    public record RegionChatRoomItem(
            String chatRoomId,
            String chatRoomName,
            String locationName,
            int participantCount,
            String lastMessageAt,
            String representativePostImageUrl
    ) {
    }

    // 지역별 미참여 채팅방 목록 조회 결과
    public record RegionChatRoomList(
            List<RegionChatRoomItem> RegionChatRoomListDTO,
            String nextCursor,
            boolean hasNext
    ) {
    }

    // 마지막 메시지 정보
    public record LastMessage(
            String content,
            Long senderId,
            String senderNickname,
            String sentAt
    ) {
    }

    // 내 참여 채팅방 항목
    public record MyChatRoomItem(
            String chatRoomId,
            String chatRoomName,
            String locationName,
            LastMessage lastMessage,
            int unreadCount,
            String profileImageUrl
    ) {
    }

    // 내 참여 채팅방 목록 결과
    public record MyChatRoomList(
            List<MyChatRoomItem> MyChatRoomListDTO,
            String nextCursor,
            boolean hasNext
    ) {
    }

    // 채팅 메시지 정보
    public record ChatMessage(
            String messageId,
            Long senderId,
            String senderNickname,
            String senderProfileImageUrl,
            String content,
            String createdAt,
            String parentMessageId,  // 답장 대상 메시지 ID (없으면 null)
            boolean read
    ) {
    }

    // 채팅 메시지 목록 결과
    public record ChatMessagesList(
            List<ChatMessage> ChatMessagesListDTO,
            boolean hasMore,
            String nextCursor
    ) {
    }

    // 채팅방 알림 설정 정보
    public record ChatRoomNotificationInfo(
            String chatRoomId,
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
