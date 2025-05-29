package com.project.teama_be.domain.member.entity;

import com.project.teama_be.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recently_viewed")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE recently_viewed SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RecentlyViewed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Update
    public void updateViewedAt(LocalDateTime now) {
        this.viewedAt = now;
    }
}
