package com.project.teama_be.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "not_recommended")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotRecommended {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "target_member_id", nullable = false)
    private Long targetMemberId;
}
