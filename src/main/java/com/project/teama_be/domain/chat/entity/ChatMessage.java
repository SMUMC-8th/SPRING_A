package com.project.teama_be.domain.chat.entity;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Column(name = "content", nullable = false)
    private String content;

    // 클라이언트에서 사용할 고유 ID (UUID)
    @Column(name = "message_id", unique = true, nullable = false)
    private String messageId;

    @Column(name = "parent_message_id")
    private String parentMessageId;  // 답장 대상 메시지 ID (null이면 일반 메시지)
}
