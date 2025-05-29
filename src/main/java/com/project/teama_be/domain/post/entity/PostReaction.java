package com.project.teama_be.domain.post.entity;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.post.enums.ReactionType;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_reaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@SQLDelete(sql = "UPDATE post_reaction SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PostReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "reaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Update
    public void updateReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
