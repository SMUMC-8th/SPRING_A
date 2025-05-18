package com.project.teama_be.domain.notification.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.notification.converter.NotiConverter;
import com.project.teama_be.domain.notification.dto.NotiReqDTO;
import com.project.teama_be.domain.notification.dto.NotiResDTO;
import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.notification.service.NotiService;
import com.project.teama_be.domain.post.converter.PostConverter;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostReaction;
import com.project.teama_be.domain.post.enums.ReactionType;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.project.teama_be.domain.post.service.command.PostCommandService;
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
    private final PostCommandService postCommandService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    //fcm토큰 저장
    @PostMapping("/token")
    @Operation(summary = "FCM API by 신윤진", description = "FCM 토큰을 받아와 저장합니다.")
    public CustomResponse<NotiResDTO> saveToken(@CurrentUser AuthUser user, @RequestBody NotiReqDTO.FcmToken reqDTO) {
        notiService.saveFcmToken(user.getUserId(), reqDTO);
        return CustomResponse.onSuccess(null);
    }

//    //fcm 푸시 알림 전송
//    @PostMapping("/send/{postId}")
//    @Operation(summary = "FCM API by 신윤진", description = "FCM 게시물 좋아요 알림 테스트용 전송")
//    public CustomResponse<String> sendMessage(@CurrentUser AuthUser user, @PathVariable Long postId) throws FirebaseMessagingException {
//        postCommandService.PostLike(user, postId);
//        //여기 잠시만 좀 서비스로직좀 넣겟습니다(테스트할때만요)
//        //잠깐 string반환으로 테스트할게요...
//        Member sender = memberRepository.findById(user.getUserId()).get();  //로그인한 사용자
//        Post post = postRepository.findById(postId).isPresent() ? postRepository.findById(postId).get() : null;
//        Member receiver = post.getMember(); //가 좋아요 누른 postId에서 receiver을 추출
//        String sendMessage = notiService.sendMessage(sender, receiver, NotiType.COMMENT);
//        return CustomResponse.onSuccess(sendMessage);
//    }

//    @PostMapping("/send-chat")
//    @Operation(summary = "FCM API by 신윤진", description = "FCM 채팅 알림 테스트용 전송")
//    public CustomResponse<NotiResDTO> sendChat(@CurrentUser Member member) throws FirebaseMessagingException {
//        NotiReqDTO.FcmToken fcmToken = notiService.getFcmToken(member.getId());
//        NotiResDTO.FcmMessage message = NotiConverter.chatConvert(NotiType.LIKE, member.getNickname());
//        notiService.sendMessage(fcmToken, message.title(), message.body());
//        return CustomResponse.onSuccess(null);
//    }

    //알림 조회 목록
    @GetMapping("/get-list")
    @Operation(summary = "FCM API by 신윤진", description = "FCM 알림 조회 목록")
    public CustomResponse<NotiResDTO.NotificationPageResponseDTO> getNotifications(
            @CurrentUser AuthUser user,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Long size
    ) {
        NotiResDTO.NotificationPageResponseDTO response = notiService.getNotifications(user.getUserId(), cursor, size);
        return CustomResponse.onSuccess(response);
    }

    //알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public CustomResponse<String> markNotificationAsRead(
            @CurrentUser AuthUser user,
            @PathVariable Long notificationId
    ) {
        notiService.markAsRead(user.getUserId(), notificationId);
        return CustomResponse.onSuccess("알림 읽음 처리 완료");
    }


    //알림 삭제, 읽지 않은 알림 조회 빼겠습니다
}

