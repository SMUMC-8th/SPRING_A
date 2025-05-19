package com.project.teama_be.domain.chat.controller;

import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.service.query.ChatQueryService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.annotation.CurrentUser;
import com.project.teama_be.global.security.userdetails.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "채팅 관련 API", description = "채팅 관련 API입니다.")
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatQueryService chatQueryService;

    @GetMapping("/sendbird-token")
    @Operation(summary = "SendBird 토큰 발급 API", description = "현재 로그인한 사용자의 SendBird 토큰을 발급합니다.")
    public Mono<CustomResponse<ChatResDTO.SendBirdTokenInfo>> getSendBirdToken(@CurrentUser AuthUser authUser) {
        log.info("SendBird 토큰 발급 요청 - 사용자: {}", authUser.getLoginId());
        Mono<ChatResDTO.SendBirdTokenInfo> tokenMono = chatQueryService.getSendBirdToken(authUser.getUserId());
        return tokenMono.map(CustomResponse::onSuccess);
    }
}
