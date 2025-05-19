package com.project.teama_be.domain.chat.converter;

import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.entity.ChatParticipant;
import com.project.teama_be.domain.chat.entity.ChatRoom;
import com.project.teama_be.domain.location.entity.Location;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomConverter {

    /**
     * ChatRoom 엔티티를 ChatRoomInfo DTO로 변환
     * 채팅방 생성/입장 API 응답용
     */
    public static ChatResDTO.ChatRoom toChatRoomInfo(ChatRoom chatRoom) {
        Location location = chatRoom.getLocation();
        String locationName = getLocationName(location);

        return ChatResDTO.ChatRoom.builder()
                .chatRoomId("location_" + location.getId())
                .chatRoomName(chatRoom.getName())
                .placeName(locationName)
                .participantCount(chatRoom.getParticipants().size())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    /**
     * 위치 정보에서 표시할 이름 추출
     * 장소명이 있으면 장소명, 없으면 주소명 사용
     */
    private static String getLocationName(Location location) {
        if (location.getPlaceName() != null && !location.getPlaceName().isEmpty()) {
            return location.getPlaceName();
        }
        return location.getAddressName();
    }

    public static ChatResDTO.RegionChatRoomItem toRegionChatRoomItem(ChatRoom chatRoom) {
        Location location = chatRoom.getLocation();
        String locationName = location.getPlaceName() != null ?
                location.getPlaceName() : location.getAddressName();

        return ChatResDTO.RegionChatRoomItem.builder()
                .chatRoomId("location_" + location.getId())
                .chatRoomName(chatRoom.getName())
                .locationName(locationName)
                .participantCount(chatRoom.getParticipants().size())
                .lastMessageAt(chatRoom.getLastMessageAt() != null ?
                        chatRoom.getLastMessageAt().toString() : chatRoom.getCreatedAt().toString())
                .representativePostImageUrl(chatRoom.getChatRoomImageUrl())
                .build();
    }

    public static ChatResDTO.ChatRoomNotificationInfo toChatRoomNotificationInfo(
            String chatRoomId, boolean notificationEnabled) {
        return ChatResDTO.ChatRoomNotificationInfo.builder()
                .chatRoomId(chatRoomId)
                .notificationEnabled(notificationEnabled)
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }
}
