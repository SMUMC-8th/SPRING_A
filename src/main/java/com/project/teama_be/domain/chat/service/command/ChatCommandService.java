package com.project.teama_be.domain.chat.service.command;

import com.project.teama_be.domain.chat.converter.ChatRoomConverter;
import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.entity.ChatParticipant;
import com.project.teama_be.domain.chat.entity.ChatRoom;
import com.project.teama_be.domain.chat.exception.ChatErrorCode;
import com.project.teama_be.domain.chat.exception.ChatException;
import com.project.teama_be.domain.chat.repository.ChatParticipantRepository;
import com.project.teama_be.domain.chat.repository.ChatRoomRepository;
import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.location.repository.LocationRepository;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final LocationRepository locationRepository;
    private final MemberRepository memberRepository;
    private final SendBirdService sendBirdService;

    /**
     * 채팅방 생성 또는 입장
     */
    @Transactional
    public ChatResDTO.ChatRoom createOrJoinChatRoom(Long locationId, Long memberId) {
        log.info("채팅방 생성/입장 요청 - 위치 ID: {}, 사용자 ID: {}", locationId, memberId);

        // 위치 정보 조회
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.LOCATION_NOT_FOUND));

        // 사용자 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 채팅방 존재 여부 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByLocationId(locationId);

        ChatRoom chatRoom;
        boolean isNewRoom = false;

        if (existingRoom.isEmpty()) {
            // 새 채팅방 생성
            chatRoom = ChatRoom.builder()
                    .location(location)
                    .name(location.getPlaceName() != null ? location.getPlaceName() : "채팅방")
                    .isActive(true)
                    .lastMessageAt(LocalDateTime.now())
                    .build();

            chatRoomRepository.save(chatRoom);
            isNewRoom = true;
        } else {
            chatRoom = existingRoom.get();
        }

        // 참여 정보 확인
        Optional<ChatParticipant> existingParticipant =
                chatParticipantRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), memberId);

        if (existingParticipant.isEmpty()) {
            // 새로 참여
            ChatParticipant participant = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .member(member)
                    .joinedAt(LocalDateTime.now())
                    .notificationEnabled(true)
                    .build();

            chatParticipantRepository.save(participant);
        }

        // 새 채팅방인 경우 SendBird 채널 생성 (동기 호출)
        if (isNewRoom) {
            try {
                String channelUrl = sendBirdService.createOpenChannel(
                                locationId,
                                chatRoom.getName(),
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAddressName())
                        .block(Duration.ofSeconds(5)); // 최대 5초 대기

                log.info("SendBird 채널 생성 성공: {}", channelUrl);
            } catch (Exception e) {
                // SendBird 채널 생성 실패해도 계속 진행
                log.error("SendBird 채널 생성 실패: {}", e.getMessage());
            }
        }

        return ChatRoomConverter.toChatRoomInfo(chatRoom);
    }

    /**
     * 채팅방 알림 설정 변경
     */
    @Transactional
    public ChatResDTO.ChatRoomNotificationInfo updateNotificationSetting(
            Long chatRoomId, Long memberId, boolean notificationEnabled) {

        log.info("채팅방 알림 설정 변경 - 채팅방 ID: {}, 사용자 ID: {}, 활성화: {}",
                chatRoomId, memberId, notificationEnabled);

        // 참여 정보 조회
        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.PARTICIPANT_NOT_FOUND));

        // 알림 설정 변경
        participant.updateNotificationEnabled(notificationEnabled);
        chatParticipantRepository.save(participant);

        return ChatRoomConverter.toChatRoomNotificationInfo(chatRoomId, notificationEnabled);
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long memberId) {
        log.info("채팅방 나가기 - 채팅방 ID: {}, 사용자 ID: {}", chatRoomId, memberId);

        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.PARTICIPANT_NOT_FOUND));

        // 참여 정보 삭제
        chatParticipantRepository.delete(participant);
    }
}