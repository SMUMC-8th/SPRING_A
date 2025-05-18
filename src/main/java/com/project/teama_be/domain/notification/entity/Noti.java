package com.project.teama_be.domain.notification.entity;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.notification.enums.NotiStatus;
import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Noti extends BaseEntity {
//firebase에서 제공하는 Notification임포트 할 때 중복돼서 Noti로 변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  //알림을 받는 사람: receiver

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;
    //content지우고 title, body, isRead 생성

    @Column(name = "isRead", nullable = false)
    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotiStatus status = NotiStatus.FAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotiType type;

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}

