package com.project.teama_be.domain.notification.service;

import com.google.firebase.messaging.*;
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
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.global.security.userdetails.AuthUser;
import com.project.teama_be.global.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotiService {

    private final RedisUtil redisUtil;
    private final NotiRepository notiRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public NotiResDTO.SaveFcmToken saveFcmToken(Long memberId, NotiReqDTO.FcmToken fcmToken) {
        String key = "fcm:" + memberId;
        redisUtil.saveFcmToken(key, fcmToken);
        String savedFcmToken = redisUtil.getFcmToken(key);
        return NotiConverter.toSaveFcmTokenResDTO(memberId, savedFcmToken);
    }

    public String getFcmToken(Long memberId) {
        String key = "fcm:" + memberId;
        String value = redisUtil.getFcmToken(key);
        if (value == null) {
            throw new NotiException(NotiErrorCode.TOKEN_NOT_FOUND);
        }
        return value;
    }

    //        NotiResDTO.FcmMessage message = NotiConverter.postConvert(NotiType.LIKE, member.getNickname());
    //        notiService.sendMessage(notiService.getFcmToken(user.getUserId()), message.title(), message.body());

    //sender:좋아요 누른 사람, receiver:해당 게시물/댓글의 주인, post:receiver의 게시물
    public NotiResDTO.NotificationSendResDTO sendMessage(Member sender, Member receiver, Post post, NotiType type) throws FirebaseMessagingException {
        String senderName = sender.getNickname();   //보내는 사람
        NotiResDTO.FcmMessage message = notiMessageConverter(type, senderName);

        // 1. FCM 토큰 조회
        String fcmToken = getFcmToken(receiver.getId()); //record라서 .fctToken()

        // 2. FCM 메시지 전송
        String fcmMessageId = makeMessage(fcmToken, message.title(), message.body());
        log.info("[ fcm 알림 전송 성공 ]: {}", fcmMessageId);
        // title에 보내는 사람, body에 님이 댓글/좋아요를 달았습니다. 가 들어가는중

        // 3. 알림 저장(알림을 받는 사람 기준)
        Noti noti = NotiConverter.toNoty(receiver, post, type, message.title(), message.body());
        try {
            notiRepository.save(noti);
        } catch (Exception e) {
            throw new NotiException(NotiErrorCode.NOTI_NOT_SAVE);
        }
        log.info("[ fcm 알림 저장 ]: {}", noti);

        return NotiConverter.toSendResDTO(noti, fcmMessageId);
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
            throw new NotiException(NotiErrorCode.FCM_SEND_FAIL2);
        }
    }

    public static NotiResDTO.FcmMessage notiMessageConverter(NotiType type, String senderName) {

        return switch (type) {
            case LIKE -> new NotiResDTO.FcmMessage(
                    senderName, "님이 회원님의 게시물을 좋아합니다");
            case COMMENT -> new NotiResDTO.FcmMessage(
                    senderName, "님이 회원님의 게시물에 댓글을 달았습니다");
            case COMMENT_COMMENT -> new NotiResDTO.FcmMessage(
                    senderName, "님이 회원님의 게시물에 대댓글을 달았습니다");
            case COMMENT_LIKE -> new NotiResDTO.FcmMessage(
                    senderName, "님이 회원님의 댓글을 좋아합니다");
//            //채팅방의 경우 senderName에 채팅방 이름을 담기
//            case CHAT -> new NotiResDTO.FcmMessage(
//                    senderName,"새로운 채팅 내용이 있습니다.");
            default -> throw new NotiException(NotiErrorCode.NOT_APPLY_NOTI);
        };
    }

    //아래는 채팅방이름, 보낸사람, 채팅내용까지입니다...
    public static NotiResDTO.FcmMessage chatConvert(String roomName, String senderName, String messagePreview) {
        return new NotiResDTO.FcmMessage(
                "[" + roomName + "] 새로운 채팅 내용",
                senderName + ": " + messagePreview
        );
    }

    /// 나중에 채팅방 구현시 이 sendChatMessage를 사용하시면 돼요
    @Transactional
    public void sendChatMessage(Member sender, List<Member> receivers, String roomName, String messagePreview) {
        NotiResDTO.FcmMessage message = NotiService.chatConvert(roomName, sender.getNickname(), messagePreview);

        List<String> tokens = new ArrayList<>();
        List<Long> noTokenUserIds = new ArrayList<>();

        for (Member receiver : receivers) {
            if (receiver.getId().equals(sender.getId())) continue; // 본인 제외

            // 아래에서 fcm토큰을 하나씩 받아서 list에 add
            String key = "fcm:" + receiver.getId();
            String  fcmToken = redisUtil.getFcmToken(key);
            if (fcmToken != null) {
                tokens.add(fcmToken);
            } else {    //fcm토큰 null인 경우 여기에 담아서 로그에 출력
                noTokenUserIds.add(receiver.getId());
            }

            // 알림 저장  - 채팅방번호라던지 같이 저장해야할거같은데 Noti 엔티티도 수정해야됨 ....
            Noti noti = NotiConverter.toChatNoti(receiver, NotiType.CHAT, message.title(), message.body());
            try {
                notiRepository.save(noti);
            } catch (Exception e) {
                throw new NotiException(NotiErrorCode.NOTI_NOT_SAVE);
            }

        }

        if (!tokens.isEmpty()) {
            sendMulticastMessage(tokens, message.title(), message.body());
        } else {
            log.error("[ FCM 멀티캐스트 실패 - 토큰 없음 ] 대상 IDs: {}", noTokenUserIds);
            throw new NotiException(NotiErrorCode.TOKEN_NOT_FOUND);
        }
    }

    //BatchResponse는 firebase에서 제공함  - 채팅방 multicast용
    public BatchResponse sendMulticastMessage(List<String> fcmTokens, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            List<Message> messages = fcmTokens.stream()
                    .map(token -> Message.builder()
                            .setToken(token)
                            .setNotification(notification)
                            .build())
                    .toList();

            return FirebaseMessaging.getInstance().sendAll(messages);
        } catch (FirebaseMessagingException e) {
            log.error("[ FCM 멀티캐스트 전송 실패 ]", e);
            throw new NotiException(NotiErrorCode.FCM_SEND_FAIL3);
        }
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Noti noti = notiRepository.findById(notificationId)
                .orElseThrow(() -> new NotiException(NotiErrorCode.NOTIFICATION_NOT_FOUND));

        Long receiverId = noti.getMember().getId();
        if (!receiverId.equals(userId)) {
            log.error("[ id 정보가 올바르지 않음 ]");
            throw new NotiException(NotiErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        if (noti.getIsRead() == false) {
            noti.setIsRead(true);
        }
    }

    // 알림 목록 조회 (10)
    @Transactional(readOnly = true)
    public NotiResDTO.NotificationPageResponseDTO getNotifications(Long userId, Long cursor, Long size) {
        int pageSize = size.intValue() + 1; // 다음 페이지 여부 확인을 위해 +1
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        List<Noti> fetched = notiRepository.findByMemberIdWithCursor(userId, cursor, pageable);

        boolean hasNext = fetched.size() > size;
        List<Noti> content = hasNext ? fetched.subList(0, size.intValue()) : fetched;

        List<NotiResDTO.NotificationListResDTO> notiDtos = content.stream()
                .map(NotiConverter::toNotiResDto)
                .toList();

        Long lastId = content.isEmpty() ? null : content.get(content.size() - 1).getId();

        return new NotiResDTO.NotificationPageResponseDTO(notiDtos, hasNext, lastId);
    }
}



