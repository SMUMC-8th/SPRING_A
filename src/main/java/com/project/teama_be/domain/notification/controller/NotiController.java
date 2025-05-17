package com.project.teama_be.domain.notification.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.notification.converter.NotiConverter;
import com.project.teama_be.domain.notification.dto.NotiReqDTO;
import com.project.teama_be.domain.notification.dto.NotiResDTO;
import com.project.teama_be.domain.notification.enums.NotificationType;
import com.project.teama_be.domain.notification.service.NotiService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.annotation.CurrentUser;
import com.project.teama_be.global.security.userdetails.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
@Tag(name = "FCM알림 API")
public class NotiController {

    private final NotiService notiService;

    //fcm토큰 저장
    @PostMapping("/token")
    @Operation(summary = "FCM API by 신윤진", description = "FCM 토큰을 받아와 저장합니다.")
    public CustomResponse<NotiResDTO> saveToken(@CurrentUser AuthUser user, @RequestBody NotiReqDTO.FcmToken reqDTO) {
        notiService.saveFcmToken(user.getUserId(), reqDTO);
        return CustomResponse.onSuccess(null);
    }

    //fcm 푸시 알림 전송
    //여기 지금 Member인데 코드좀 합치고 나서 service단에서 member반환하는 거 추가하고 user로 바꿔서 하겟습니다
    @PostMapping("/send")
    @Operation(summary = "FCM API by 신윤진", description = "FCM 게시물/댓글 알림 테스트용 전송")
    public CustomResponse<NotiResDTO> sendMessage(@CurrentUser Member member) throws FirebaseMessagingException {
        NotiResDTO.FcmMessage message = NotiConverter.postConvert(NotificationType.LIKE, member.getNickname());
        notiService.sendMessage(member.getId(), message.title(), message.body());
        return CustomResponse.onSuccess(null);
    }

//    @PostMapping("/send-chat")
//    @Operation(summary = "FCM API by 신윤진", description = "FCM 채팅 알림 테스트용 전송")
//    public CustomResponse<NotiResDTO> sendChat(@CurrentUser Member member) throws FirebaseMessagingException {
//        NotiReqDTO.FcmToken fcmToken = notiService.getFcmToken(member.getId());
//        NotiResDTO.FcmMessage message = NotiConverter.chatConvert(NotificationType.LIKE, member.getNickname());
//        notiService.sendMessage(fcmToken, message.title(), message.body());
//        return CustomResponse.onSuccess(null);
//    }

    //알림 조회 목록
    @GetMapping("/get-list")
    @Operation(summary = "FCM API by 신윤진", description = "FCM 알림 조회 목록")
    public CustomResponse<NotiResDTO.NotificationPageResponseDTO> getNotifications(
            @CurrentUser Member member,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    //알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public CustomResponse<Void> markNotificationAsRead(
            @CurrentUser AuthUser user,
            @PathVariable Long notificationId
    ) {
        notiService.markAsRead(user.getUserId(), notificationId);
        return CustomResponse.onSuccess(null);
    }


    //알림 삭제, 읽지 않은 알림 조회 빼겠습니다
}

