package com.project.teama_be.domain.post.entity;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@SQLDelete(sql = "UPDATE comment SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "parent_id")
    @Builder.Default
    private Long parentId = 0L;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentReaction> reactions = new ArrayList<>();

    // update
    public void updateLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
