package com.project.teama_be.domain.notification.converter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.notification.dto.NotiResDTO;
import com.project.teama_be.domain.notification.entity.Noti;
import com.project.teama_be.domain.notification.enums.NotiStatus;
import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.notification.exception.NotiException;
import com.project.teama_be.domain.notification.exception.code.NotiErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotiConverter {

    public Noti toEntity(Member member, NotiType type, String title, String body) {
        return Noti.builder()
                .member(member)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .status(NotiStatus.PENDING)
                .build();
    }

//    public static NotiResDTO.FcmMessage postConvert(NotiType type, String senderName) {
//
//        return switch (type) {
//            case LIKE -> new NotiResDTO.FcmMessage(
//                    senderName + "님이 회원님의 게시물을 좋아합니다",
//                    ""
//            );
//            case COMMENT -> new NotiResDTO.FcmMessage(
//                    senderName + "님이 회원님의 게시물에 댓글을 달았습니다",
//                    ""
//            );
//            //채팅방의 경우 senderName에 채팅방 이름을 담기
//            case CHAT -> new NotiResDTO.FcmMessage(
//                    senderName,
//                    "새로운 채팅 내용"
//            );
//            default -> throw new NotiException(NotiErrorCode.NOT_APPLY_NOTI);
//        };
//    }
//
//    //여기에 채팅방 이미지?까지 넣을까요 아니면 그냥 위에처럼 채팅방이름 / 새로운 채팅내용 까지만 알림을 띄울까요?
//    //아래는 채팅방이름, 보낸사람, 채팅내용까지입니다...
//    public static NotiResDTO.FcmMessage chatConvert(String roomName, String senderName, String messagePreview) {
//        return new NotiResDTO.FcmMessage(
//                "[" + roomName + "] 새로운 채팅 내용",
//                senderName + ": " + messagePreview
//        );
//    }
}
