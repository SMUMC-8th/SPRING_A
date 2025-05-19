package com.project.teama_be.domain.chat.controller;

import com.project.teama_be.domain.chat.dto.request.ChatReqDTO;
import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.service.command.ChatCommandService;
import com.project.teama_be.domain.chat.service.query.ChatQueryService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.annotation.CurrentUser;
import com.project.teama_be.global.security.userdetails.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "채팅 관련 API", description = "채팅 관련 API입니다.")
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;

    @GetMapping("/sendbird-token")
    @Operation(summary = "SendBird 토큰 발급 API", description = "현재 로그인한 사용자의 SendBird 토큰을 발급합니다.")
    public Mono<CustomResponse<ChatResDTO.SendBirdTokenInfo>> getSendBirdToken(@CurrentUser AuthUser authUser) {
        log.info("SendBird 토큰 발급 요청 - 사용자: {}", authUser.getLoginId());
        Mono<ChatResDTO.SendBirdTokenInfo> tokenMono = chatQueryService.getSendBirdToken(authUser.getUserId());
        return tokenMono.map(CustomResponse::onSuccess);
    }

    @GetMapping("/rooms/region/non-participating")
    @Operation(summary = "지역별 미참여 채팅방 목록 조회 API", description = "내가 참여하지 않은 지역별 채팅방 목록을 조회합니다.")
    public Mono<CustomResponse<ChatResDTO.RegionChatRoomList>> getNonParticipatingRegionChatRooms(
            @RequestParam String region,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @CurrentUser AuthUser authUser) {

        log.info("지역별 미참여 채팅방 목록 조회 - 지역: {}, 사용자: {}", region, authUser.getLoginId());
        return chatQueryService.getNonParticipatingRegionChatRooms(region, authUser.getUserId(), cursor, limit)
                .map(CustomResponse::onSuccess);
    }

    @GetMapping("/rooms/my")
    @Operation(summary = "내 참여 채팅방 목록 조회 API", description = "내가 참여한 채팅방 목록을 조회합니다.")
    public Mono<CustomResponse<ChatResDTO.RegionChatRoomList>> getMyChatRooms(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @CurrentUser AuthUser authUser) {

        log.info("내 참여 채팅방 목록 조회 - 사용자: {}", authUser.getLoginId());
        return chatQueryService.getMyChatRooms(authUser.getUserId(), cursor, limit)
                .map(CustomResponse::onSuccess);
    }

    @PutMapping("/rooms/{chatRoomId}/notification")
    @Operation(summary = "채팅방 알림 설정 API", description = "채팅방의 알림 설정을 변경합니다.")
    public Mono<CustomResponse<ChatResDTO.ChatRoomNotificationInfo>> updateNotification(
            @PathVariable String chatRoomId,
            @RequestBody ChatReqDTO.UpdateChatRoomNotification reqDTO,
            @CurrentUser AuthUser authUser) {

        log.info("채팅방 알림 설정 변경 - 채팅방: {}, 설정: {}", chatRoomId, reqDTO.notificationEnabled());
        return chatCommandService.updateNotificationSetting(chatRoomId, authUser.getUserId(), reqDTO.notificationEnabled())
                .map(CustomResponse::onSuccess);
    }
}
