package com.project.teama_be.domain.notification.converter;

import com.project.teama_be.domain.notification.dto.NotiResDTO;
import com.project.teama_be.domain.notification.enums.NotificationType;
import com.project.teama_be.domain.notification.exception.NotificationException;
import com.project.teama_be.domain.notification.exception.code.NotificationErrorCode;
import org.springframework.stereotype.Component;

@Component
public class NotiConverter {


    public static NotiResDTO.FcmMessage postConvert(NotificationType type, String senderName) {


        return switch (type) {
            case LIKE -> new NotiResDTO.FcmMessage(
                    senderName + "님이 회원님의 게시물을 좋아합니다",
                    ""
            );
            case COMMENT -> new NotiResDTO.FcmMessage(
                    senderName + "님이 회원님의 게시물에 댓글을 달았습니다",
                    ""
            );
            //채팅방의 경우 senderName에 채팅방 이름을 담기
            case CHAT -> new NotiResDTO.FcmMessage(
                    senderName,
                    "새로운 채팅 내용"
            );
            default -> throw new NotificationException(NotificationErrorCode.NOT_APPLY_NOTI);
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
}
