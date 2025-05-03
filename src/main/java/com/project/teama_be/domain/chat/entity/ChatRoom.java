package com.project.teama_be.domain.chat.entity;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "name")
    private String name;

    @Column(name = "chat_room_image_url")
    private String chatRoomImageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}
