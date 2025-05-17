package com.project.teama_be.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.project.teama_be.domain.notification.converter.NotiConverter;
import com.project.teama_be.domain.notification.dto.NotiReqDTO;
import com.project.teama_be.domain.notification.entity.Noti;

import com.project.teama_be.domain.notification.exception.NotificationException;
import com.project.teama_be.domain.notification.exception.code.NotificationErrorCode;
import com.project.teama_be.domain.notification.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;


    @Transactional
    public void saveFcmToken(Long memberId, NotiReqDTO.FcmToken fcmToken) {
        String key = "fcm:" + memberId;
        redisUtil.saveFcmToken(key, fcmToken);
    }

    public NotiReqDTO.FcmToken getFcmToken(Long memberId) {
        String key = "fcm:" + memberId;
        NotiReqDTO.FcmToken value = redisUtil.getFcmToken(key);
        if (value == null) {
            throw new NotificationException(NotificationErrorCode.TOKEN_NOT_FOUND);
        }
        return value;
    }

    //        NotiResDTO.FcmMessage message = NotiConverter.postConvert(NotificationType.LIKE, member.getNickname());
    //        notiService.sendMessage(notiService.getFcmToken(user.getUserId()), message.title(), message.body());

//아래 부분 주헌님 코드 참고했는데 이걸 member>sevice에 memberSevice로 만들어서 같이 쓰는게 좋을거같아요
//    private Member getMember(AuthUser user) {
//
//        Member member = memberRepository.findByLoginId(user.getLoginId()).orElseThrow(()->
//                new PostException(PostErrorCode.USER_NOT_FOUND));
//        log.info("[ 유저 정보 생성 ] member:{}", member);
//        return member;
//    }


    public void sendMessage(Long id, String title, String body) throws FirebaseMessagingException {
        NotiReqDTO.FcmToken fcmToken = getFcmToken(id);
        try {
            // 메시지 생성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken.fcmToken())  // FCM Token 사용
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("[ FCM 전송 실패 ]", e);
            throw new NotificationException(NotificationErrorCode.FCM_SEND_FAIL);
        }
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Noti noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        Long receiverId = noti.getMember().getId();
        if (!receiverId.equals(userId)) {
            log.error("[ id 정보가 올바르지 않음 ]");
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        if (!noti.getIsRead()) {
            noti.setIsRead(true);
        }
    }
}



