package com.project.teama_be.domain.notification.converter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.notification.dto.NotiResDTO;
import com.project.teama_be.domain.notification.entity.Noti;
import com.project.teama_be.domain.notification.enums.NotiStatus;
import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.post.entity.Post;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotiConverter {

    public static Noti toNoty(Member member, Post post, NotiType type, String title, String body) {
        return Noti.builder()
                .member(member)
                .post(post)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .status(NotiStatus.SUCCESS)
                .build();
    }

    public static Noti toChatNoti(Member member, NotiType type, String title, String body) {
        return Noti.builder()
                .member(member)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .status(NotiStatus.SUCCESS)
                .build();
    }

    public static NotiResDTO.NotificationSendResDTO toSendResDTO(Noti noti, String fcmMessageId) {
        return new NotiResDTO.NotificationSendResDTO(
                noti.getTitle(),
                noti.getBody(),
                fcmMessageId,
                noti.getId()
        );
    }

    public static NotiResDTO.NotificationListResDTO toNotiResDto(Noti noti) {
        return new NotiResDTO.NotificationListResDTO(
                noti.getId(),
                noti.getTitle(),
                noti.getBody(),
                noti.getIsRead(),
                noti.getCreatedAt()
        );
    }

    public static NotiResDTO.SaveFcmToken toSaveFcmTokenResDTO(Long memberId, String fcmToken) {
        return NotiResDTO.SaveFcmToken.builder()
                .memberId(memberId)
                .fcmToken(fcmToken)
                .build();
    }
}
