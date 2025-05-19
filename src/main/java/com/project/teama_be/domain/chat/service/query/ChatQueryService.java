package com.project.teama_be.domain.chat.service.query;

import com.project.teama_be.domain.chat.converter.ChatRoomConverter;
import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.repository.ChatRoomRepository;
import com.project.teama_be.domain.chat.service.command.SendBirdService;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    public Mono<ChatResDTO.SendBirdTokenInfo> getSendBirdToken(Long memberId) {
        log.info("SendBird 토큰 발급 요청 - 사용자 ID: {}", memberId);

        return Mono.fromCallable(() ->
                        memberRepository.findById(memberId)
                                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)))
                // fromCallable 내부 로직을 Spring WebFlux의 boundedElastic 스레드 풀에서 실행하도록 설정
                // DB 접근처럼 블로킹 작업에 적합한 스케줄러
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(sendBirdService::getUserToken);
    }

    /**
     * 지역별 미참여 채팅방 목록 조회
     */
    public Mono<ChatResDTO.RegionChatRoomList> getNonParticipatingRegionChatRooms(
            String region, Long memberId, Long cursor, Integer limit) {

        log.info("지역별 미참여 채팅방 목록 조회 - 지역: {}, 사용자 ID: {}, 커서: {}, 제한: {}",
                region, memberId, cursor, limit);

        int pageSize = limit != null ? limit : 10;

        return Mono.fromCallable(() -> {
                    Pageable pageable = PageRequest.of(0, pageSize);

                    // 참여하지 않은 채팅방만 조회
                    List<ChatResDTO.RegionChatRoomItem> chatRooms = chatRoomRepository
                            .findNonParticipatingRoomsByRegion(region, memberId, pageable)
                            .stream()
                            .map(ChatRoomConverter::toRegionChatRoomItem)
                            .collect(Collectors.toList());

                    // 커서 기반 페이징 처리
                    String nextCursor = chatRooms.size() >= pageSize && !chatRooms.isEmpty() ?
                            chatRooms.get(chatRooms.size() - 1).chatRoomId() : null;
                    boolean hasNext = nextCursor != null;

                    return ChatResDTO.RegionChatRoomList.builder()
                            .RegionChatRoomListDTO(chatRooms)
                            .nextCursor(nextCursor)
                            .hasNext(hasNext)
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
