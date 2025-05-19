package com.project.teama_be.domain.chat.service.query;

import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.service.command.SendBirdService;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatQueryService {

    private final MemberRepository memberRepository;
    private final SendBirdService sendBirdService;

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
}
