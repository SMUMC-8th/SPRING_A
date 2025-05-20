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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "채팅 관련 API", description = "채팅 관련 API입니다.")
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;

    @PostMapping("/rooms")
    @Operation(summary = "채팅방 생성/입장 API by 김지명", description = "위치 기반 채팅방을 생성하거나 입장합니다.")
    public ResponseEntity<CustomResponse<ChatResDTO.ChatRoom>> createChatRoom(
            @RequestBody ChatReqDTO.CreateChatRoom reqDTO,
            @CurrentUser AuthUser authUser) {

        log.info("채팅방 생성/입장 요청 - 위치 ID: {}, 사용자: {}", reqDTO.placeId(), authUser.getLoginId());
        ChatResDTO.ChatRoom result = chatCommandService.createOrJoinChatRoom(reqDTO.placeId(), authUser.getUserId());
        return ResponseEntity.ok(CustomResponse.onSuccess(result));
    }

    @GetMapping("/sendbird-token")
    @Operation(summary = "SendBird 토큰 발급 API by 김지명", description = "현재 로그인한 사용자의 SendBird 토큰을 발급합니다.")
    public ResponseEntity<CustomResponse<ChatResDTO.SendBirdTokenInfo>> getSendBirdToken(@CurrentUser AuthUser authUser) {
        log.info("SendBird 토큰 발급 요청 - 사용자: {}", authUser.getLoginId());
        ChatResDTO.SendBirdTokenInfo tokenInfo = chatQueryService.getSendBirdToken(authUser.getUserId());
        return ResponseEntity.ok(CustomResponse.onSuccess(tokenInfo));
    }

    @GetMapping("/rooms/region/non-participating")
    @Operation(summary = "지역별 미참여 채팅방 목록 조회 API by 김지명", description = "내가 참여하지 않은 지역별 채팅방 목록을 조회합니다.")
    public ResponseEntity<CustomResponse<ChatResDTO.ChatRoomList>> getNonParticipatingRegionChatRooms(
            @RequestParam String region,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @CurrentUser AuthUser authUser) {

        log.info("지역별 미참여 채팅방 목록 조회 - 지역: {}, 사용자: {}", region, authUser.getLoginId());
        ChatResDTO.ChatRoomList result = chatQueryService.getNonParticipatingRegionChatRooms(
                region, authUser.getUserId(), cursor, limit);
        return ResponseEntity.ok(CustomResponse.onSuccess(result));
    }

    @GetMapping("/rooms/my")
    @Operation(summary = "내 참여 채팅방 목록 조회 API by 김지명", description = "내가 참여한 채팅방 목록을 조회합니다.")
    public ResponseEntity<CustomResponse<ChatResDTO.ChatRoomList>> getMyChatRooms(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @CurrentUser AuthUser authUser) {

        log.info("내 참여 채팅방 목록 조회 - 사용자: {}", authUser.getLoginId());
        ChatResDTO.ChatRoomList result = chatQueryService.getMyChatRooms(
                authUser.getUserId(), cursor, limit);
        return ResponseEntity.ok(CustomResponse.onSuccess(result));
    }

    @PatchMapping("/rooms/{chatRoomId}/notification")
    @Operation(summary = "채팅방 알림 설정 API by 김지명", description = "채팅방의 알림 설정을 변경합니다.")
    public ResponseEntity<CustomResponse<ChatResDTO.ChatRoomNotificationInfo>> updateNotification(
            @PathVariable Long chatRoomId,
            @RequestBody ChatReqDTO.UpdateChatRoomNotification reqDTO,
            @CurrentUser AuthUser authUser) {

        log.info("채팅방 알림 설정 변경 - 채팅방: {}, 설정: {}", chatRoomId, reqDTO.notificationEnabled());
        ChatResDTO.ChatRoomNotificationInfo result = chatCommandService.updateNotificationSetting(
                chatRoomId, authUser.getUserId(), reqDTO.notificationEnabled());
        return ResponseEntity.ok(CustomResponse.onSuccess(result));
    }

    @DeleteMapping("/rooms/{chatRoomId}")
    @Operation(summary = "채팅방 나가기 API by 김지명", description = "채팅방에서 나갑니다.")
    public ResponseEntity<CustomResponse<String>> leaveChatRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser AuthUser authUser) {
        log.info("채팅방 나가기 - 채팅방: {}", chatRoomId);
        chatCommandService.leaveChatRoom(chatRoomId, authUser.getUserId());
        return ResponseEntity.ok(CustomResponse.onSuccess("채팅방을 나갔습니다."));
    }
}