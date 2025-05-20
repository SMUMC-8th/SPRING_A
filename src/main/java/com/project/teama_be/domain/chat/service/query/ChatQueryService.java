package com.project.teama_be.domain.chat.service.query;

import com.project.teama_be.domain.chat.converter.ChatRoomConverter;
import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.exception.ChatErrorCode;
import com.project.teama_be.domain.chat.exception.ChatException;
import com.project.teama_be.domain.chat.repository.ChatRoomRepository;
import com.project.teama_be.domain.chat.service.command.SendBirdService;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatQueryService {

    private final MemberRepository memberRepository;
    private final SendBirdService sendBirdService;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * SendBird 토큰 발급
     */
    public ChatResDTO.SendBirdTokenInfo getSendBirdToken(Long memberId) {
        log.info("SendBird 토큰 발급 요청 - 사용자 ID: {}", memberId);

        try {
            // 회원 정보 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

            // SendBird 토큰 발급 (WebFlux의 block 메서드 사용)
            return sendBirdService.getUserToken(member)
                    .block(Duration.ofSeconds(5)); // 최대 5초 대기
        } catch (Exception e) {
            log.error("SendBird 토큰 발급 처리 중 오류: {}", e.getMessage());
            throw new ChatException(ChatErrorCode.SENDBIRD_TOKEN_ERROR);
        }
    }

    /**
     * 지역별 미참여 채팅방 목록 조회
     */
    public ChatResDTO.ChatRoomList getNonParticipatingRegionChatRooms(
            String region, Long memberId, Long cursor, Integer limit) {

        log.info("지역별 미참여 채팅방 목록 조회 - 지역: {}, 사용자 ID: {}, 커서: {}, 제한: {}",
                region, memberId, cursor, limit);

        int pageSize = limit != null ? limit : 10;
        Pageable pageable = PageRequest.of(0, pageSize);

        // 참여하지 않은 채팅방만 조회
        List<ChatResDTO.ChatRoomItem> chatRooms = chatRoomRepository
                .findNonParticipatingRoomsByRegion(region, memberId, pageable)
                .stream()
                .map(ChatRoomConverter::toRegionChatRoomItem)
                .collect(Collectors.toList());

        // 커서 기반 페이징 처리
        return getRegionChatRoomList(pageSize, chatRooms);
    }

    /**
     * 내 참여 채팅방 목록 조회
     */
    public ChatResDTO.ChatRoomList getMyChatRooms(
            Long memberId, Long cursor, Integer limit) {

        log.info("내 참여 채팅방 목록 조회 - 사용자 ID: {}, 커서: {}, 제한: {}", memberId, cursor, limit);

        int pageSize = limit != null ? limit : 10;
        Pageable pageable = PageRequest.of(0, pageSize);

        List<ChatResDTO.ChatRoomItem> chatRooms = chatRoomRepository
                .findParticipatingRoomsByMemberId(memberId, pageable)
                .stream()
                .map(ChatRoomConverter::toRegionChatRoomItem)
                .collect(Collectors.toList());

        return getRegionChatRoomList(pageSize, chatRooms);
    }

    private ChatResDTO.ChatRoomList getRegionChatRoomList(int pageSize, List<ChatResDTO.ChatRoomItem> chatRooms) {
        Long nextCursor = chatRooms.size() >= pageSize && !chatRooms.isEmpty() ?
                chatRooms.get(chatRooms.size() - 1).chatRoomId() : null;
        boolean hasNext = nextCursor != null;

        return ChatResDTO.ChatRoomList.builder()
                .RegionChatRoomListDTO(chatRooms)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}