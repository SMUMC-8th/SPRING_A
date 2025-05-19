package com.project.teama_be.domain.chat.service.command;

import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.exception.ChatErrorCode;
import com.project.teama_be.domain.chat.exception.ChatException;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendBirdService {

    private final WebClient sendBirdWebClient;
    private final MemberRepository memberRepository;

    @Value("${sendbird.app-id}")
    private String appId;


    /**
     * 사용자 ID로 SendBird 토큰 발급 (ChatQueryService에서 호출)
     */
    public Mono<ChatResDTO.SendBirdTokenInfo> getSendBirdToken(Long memberId) {
        // 이 메서드는 ChatQueryService에서 사용자 ID만 가지고 호출할 때 사용됩니다
        // 실제 구현에서는 사용자 정보를 조회하여 getUserToken 메서드로 위임합니다
        log.info("사용자 ID로 SendBird 토큰 발급 요청: {}", memberId);
        return Mono.empty(); // 실제 구현 시 회원 조회 후 getUserToken 호출
    }

    /**
     * SendBird 사용자 토큰 발급 (Member 객체 기반)
     */
    public Mono<ChatResDTO.SendBirdTokenInfo> getUserToken(Member member) {
        log.info("SendBird 토큰 발급 시작 - 사용자 ID: {}", member.getId());

        return createOrUpdateUser(member)
                .flatMap(response -> issueAccessToken(member.getId().toString()))
                .map(token -> ChatResDTO.SendBirdTokenInfo.builder()
                        .sendBirdUserId(member.getId().toString())
                        .sendBirdToken(token)
                        .expiredAt(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)) // 30일
                        .build())
                .doOnError(e -> {
                    log.error("SendBird 토큰 발급 실패 - 사용자 ID: {}, 오류: {}", member.getId(), e.getMessage());
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException webClientEx = (WebClientResponseException) e;
                        log.error("SendBird API 응답: {}, 상태 코드: {}",
                                webClientEx.getResponseBodyAsString(),
                                webClientEx.getStatusCode());
                    }
                    throw new ChatException(ChatErrorCode.SENDBIRD_TOKEN_ERROR);
                });
    }

    /**
     * SendBird 사용자 생성 또는 업데이트
     */
    private Mono<Map> createOrUpdateUser(Member member) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", member.getId().toString());
        body.put("nickname", member.getNickname());

        if (member.getProfileUrl() != null && !member.getProfileUrl().isEmpty()) {
            body.put("profile_url", member.getProfileUrl());
        }

        return sendBirdWebClient.post()
                .uri("/users")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 400 &&
                            ex.getResponseBodyAsString().contains("user_id already exists")) {
                        log.info("이미 존재하는 사용자, 업데이트 시도 - 사용자 ID: {}", member.getId());
                        return updateUser(member);
                    }
                    return Mono.error(ex);
                });
    }

    /**
     * 기존 SendBird 사용자 정보 업데이트
     */
    private Mono<Map> updateUser(Member member) {
        Map<String, Object> body = new HashMap<>();
        body.put("nickname", member.getNickname());

        if (member.getProfileUrl() != null && !member.getProfileUrl().isEmpty()) {
            body.put("profile_url", member.getProfileUrl());
        }

        return sendBirdWebClient.put()
                .uri("/users/" + member.getId())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class);
    }

    /**
     * SendBird 사용자 토큰 발급
     */
    private Mono<String> issueAccessToken(String userId) {
        Map<String, Object> body = new HashMap<>();
        body.put("expires_at", System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));

        return sendBirdWebClient.post()
                .uri("/users/" + userId + "/token")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("token"));
    }

    /**
     * SendBird 오픈 채널 생성
     */
    public Mono<String> createOpenChannel(Long locationId, String channelName,
                                          BigDecimal latitude, BigDecimal longitude, String address) {
        log.info("SendBird 오픈 채널 생성 - 위치 ID: {}", locationId);

        String channelUrl = "location_" + locationId;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("latitude", latitude != null ? latitude.toString() : null);
        metadata.put("longitude", longitude != null ? longitude.toString() : null);
        metadata.put("address", address);

        Map<String, Object> body = new HashMap<>();
        body.put("channel_url", channelUrl);
        body.put("name", channelName);
        body.put("metadata", metadata);

        return sendBirdWebClient.post()
                .uri("/open_channels")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    log.info("SendBird 오픈 채널 생성 성공 - 채널 URL: {}", channelUrl);
                    return channelUrl;
                })
                .doOnError(e -> {
                    log.error("SendBird 오픈 채널 생성 실패 - 위치 ID: {}, 오류: {}", locationId, e.getMessage());
                    throw new ChatException(ChatErrorCode.SENDBIRD_CHANNEL_ERROR);
                });
    }
}
