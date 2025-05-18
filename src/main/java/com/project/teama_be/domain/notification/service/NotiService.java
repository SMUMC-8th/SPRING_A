package com.project.teama_be.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.notification.converter.NotiConverter;
import com.project.teama_be.domain.notification.dto.NotiReqDTO;
import com.project.teama_be.domain.notification.dto.NotiResDTO;
import com.project.teama_be.domain.notification.entity.Noti;

import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.notification.exception.NotiException;
import com.project.teama_be.domain.notification.exception.code.NotiErrorCode;
import com.project.teama_be.domain.notification.repository.NotiRepository;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.global.security.userdetails.AuthUser;
import com.project.teama_be.global.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotiService {

    private final RedisUtil redisUtil;
    private final NotiConverter notiConverter;
    private final NotiRepository notiRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void saveFcmToken(Long memberId, NotiReqDTO.FcmToken fcmToken) {
        String key = "fcm:" + memberId;
        redisUtil.saveFcmToken(key, fcmToken);
    }

    public NotiReqDTO.FcmToken getFcmToken(Long memberId) {
        String key = "fcm:" + memberId;
        NotiReqDTO.FcmToken value = redisUtil.getFcmToken(key);
        if (value == null) {
            throw new NotiException(NotiErrorCode.TOKEN_NOT_FOUND);
        }
        return value;
    }

    //        NotiResDTO.FcmMessage message = NotiConverter.postConvert(NotiType.LIKE, member.getNickname());
    //        notiService.sendMessage(notiService.getFcmToken(user.getUserId()), message.title(), message.body());

//아래 부분 주헌님 코드 참고했는데 이걸 member>sevice에 memberSevice로 만들어서 같이 쓰는게 좋을거같아요
    private Member getMember(AuthUser user) {

        Member member = memberRepository.findByLoginId(user.getLoginId()).orElseThrow(()->
                new PostException(PostErrorCode.USER_NOT_FOUND));
        log.info("[ 유저 정보 생성 ] member:{}", member);
        return member;
    }


    public String sendMessage(Member sender, Member receiver, NotiType type) throws FirebaseMessagingException {
        String senderName = sender.getNickname();   //보내는 사람
        NotiResDTO.FcmMessage message = notiMessageConverter(type, senderName);

        // 1. 알림 전송
        String fcmToken = getFcmToken(receiver.getId()).fcmToken(); //record라서 .fctToken()
        String sendMessage = makeMessage(fcmToken, message.title(), message.body());
        // title에 보내는 사람, body에 님이 댓글/좋아요를 달았습니다. 가 들어가는중

        // 2. 알림 저장(알림을 받는 사람 기준)
        Noti noti = notiConverter.toNoty(receiver, type, message.title(), message.body());
        notiRepository.save(noti);
        log.info("[ fcm 알림 저장 ]: {}", noti);

        return sendMessage;
    }

    private String makeMessage(String fcmToken, String title, String body) {
        try {
            // 메시지 생성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)  // FCM Token 사용
                    .setNotification(notification)
                    .build();

            //메세지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("[ FCM 전송 실패 ]", e);
            throw new NotiException(NotiErrorCode.FCM_SEND_FAIL);
        }
    }

    public static NotiResDTO.FcmMessage notiMessageConverter(NotiType type, String senderName) {

        return switch (type) {
            case LIKE -> new NotiResDTO.FcmMessage(
                    senderName, "님이 회원님의 게시물을 좋아합니다");
            case COMMENT -> new NotiResDTO.FcmMessage(
                    senderName, "님이 회원님의 게시물에 댓글을 달았습니다");
            //채팅방의 경우 senderName에 채팅방 이름을 담기
            case CHAT -> new NotiResDTO.FcmMessage(
                    senderName,"새로운 채팅 내용이 있습니다.");
            default -> throw new NotiException(NotiErrorCode.NOT_APPLY_NOTI);
        };
    }

    //여기에 채팅방 이미지?까지 넣을까요 아니면 그냥 위에처럼 채팅방이름 / 새로운 채팅내용 까지만 알림을 띄울까요?
    //아래는 채팅방이름, 보낸사람, 채팅내용까지입니다...
    public static NotiResDTO.FcmMessage chatConvert(String roomName, String senderName, String messagePreview) {
        return new NotiResDTO.FcmMessage(
                "[" + roomName + "] 새로운 채팅 내용",
                senderName + ": " + messagePreview
        );
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Noti noti = notiRepository.findById(notificationId)
                .orElseThrow(() -> new NotiException(NotiErrorCode.NOTIFICATION_NOT_FOUND));

        Long receiverId = noti.getMember().getId();
        if (!receiverId.equals(userId)) {
            log.error("[ id 정보가 올바르지 않음 ]");
            throw new NotiException(NotiErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        if (!noti.getIsRead()) {
            noti.setIsRead(true);
        }
    }
}



