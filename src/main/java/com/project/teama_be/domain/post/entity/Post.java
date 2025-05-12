package com.project.teama_be.domain.post.entity;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "unlike")
    private Long unlikeCount;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(name = "disable_comment")
    private Boolean disableComment;

    @Column(name = "hide_like")
    private Boolean hideLike;

    @Column(name = "hide_share")
    private Boolean hideShare;
}
