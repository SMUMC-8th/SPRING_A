package com.project.teama_be.domain.post.entity;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.entity.RecentlyViewed;
import com.project.teama_be.domain.notification.entity.Noti;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@SQLDelete(sql = "UPDATE post SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
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
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "unlike")
    @Builder.Default
    private Long unlikeCount = 0L;

    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;

    @Column(name = "disable_comment")
    @Builder.Default
    private Boolean disableComment = false;

    @Column(name = "hide_like")
    @Builder.Default
    private Boolean hideLike = false;

    @Column(name = "hide_share")
    @Builder.Default
    private Boolean hideShare = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> postImages = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostReaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Noti> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecentlyViewed> recentlyVieweds = new ArrayList<>();

    // update
    public void updateLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateLocation(Location location) {
        this.location = location;
    }
}
