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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public Mono<ChatResDTO.ChatRoom> createOrJoinChatRoom(Long locationId, Long memberId) {
        log.info("채팅방 생성/입장 요청 - 위치 ID: {}, 사용자 ID: {}", locationId, memberId);

        return Mono.fromCallable(() -> {
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

                    return new Object[]{chatRoom, isNewRoom, location};
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(objects -> {
                    ChatRoom chatRoom = (ChatRoom) objects[0];
                    boolean isNewRoom = (boolean) objects[1];
                    Location location = (Location) objects[2];

                    if (isNewRoom) {
                        // 새 채팅방인 경우만 SendBird 채널 생성
                        return sendBirdService.createOpenChannel(
                                        locationId,
                                        chatRoom.getName(),
                                        location.getLatitude(),
                                        location.getLongitude(),
                                        location.getAddressName())
                                .map(channelUrl -> chatRoom);
                    } else {
                        // 기존 채팅방이면 바로 반환
                        return Mono.just(chatRoom);
                    }
                })
                .map(ChatRoomConverter::toChatRoomInfo);
    }

    /**
     * 채팅방 알림 설정 변경
     */
    @Transactional
    public Mono<ChatResDTO.ChatRoomNotificationInfo> updateNotificationSetting(
            String chatRoomId, Long memberId, boolean notificationEnabled) {

        log.info("채팅방 알림 설정 변경 - 채팅방 ID: {}, 사용자 ID: {}, 활성화: {}",
                chatRoomId, memberId, notificationEnabled);

        return Mono.fromCallable(() -> {
                    // 채팅방 ID에서 숫자 부분만 추출
                    Long roomId = null;
                    try {
                        roomId = Long.parseLong(chatRoomId.replace("location_", ""));
                    } catch (NumberFormatException e) {
                        throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
                    }

                    // 참여 정보 조회
                    ChatParticipant participant = chatParticipantRepository
                            .findByChatRoomIdAndMemberId(roomId, memberId)
                            .orElseThrow(() -> new ChatException(ChatErrorCode.PARTICIPANT_NOT_FOUND));

                    // 알림 설정 변경
                    participant.updateNotificationEnabled(notificationEnabled);
                    chatParticipantRepository.save(participant);

                    return ChatRoomConverter.toChatRoomNotificationInfo(chatRoomId, notificationEnabled);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public Mono<Void> leaveChatRoom(String chatRoomId, Long memberId) {
        log.info("채팅방 나가기 - 채팅방 ID: {}, 사용자 ID: {}", chatRoomId, memberId);

        return Mono.fromCallable(() -> {
                    // 채팅방 ID에서 숫자 부분만 추출
                    Long roomId = null;
                    try {
                        roomId = Long.parseLong(chatRoomId.replace("location_", ""));
                    } catch (NumberFormatException e) {
                        throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
                    }

                    // 참여 정보 조회
                    ChatParticipant participant = chatParticipantRepository
                            .findByChatRoomIdAndMemberId(roomId, memberId)
                            .orElseThrow(() -> new ChatException(ChatErrorCode.PARTICIPANT_NOT_FOUND));

                    // 참여 정보 삭제
                    chatParticipantRepository.delete(participant);

                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
