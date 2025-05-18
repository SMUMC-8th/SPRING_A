package com.project.teama_be.domain.notification.converter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.notification.entity.Noti;
import com.project.teama_be.domain.notification.enums.NotiStatus;
import com.project.teama_be.domain.notification.enums.NotiType;
import org.springframework.stereotype.Component;

@Component
public class NotiConverter {

    public Noti toNoty(Member member, NotiType type, String title, String body) {
        return Noti.builder()
                .member(member)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .status(NotiStatus.PENDING)
                .build();
    }
}
