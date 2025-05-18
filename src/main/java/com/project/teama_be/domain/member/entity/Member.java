package com.project.teama_be.domain.member.entity;

import com.project.teama_be.domain.chat.entity.ChatMessage;
import com.project.teama_be.domain.chat.entity.ChatParticipant;
import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.member.enums.LoginType;
import com.project.teama_be.domain.notification.entity.Noti;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.CommentReaction;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostReaction;
import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "is_agree", nullable = false)
    private boolean isAgree;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    // 양방향 관계 설정
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentReaction> commentReactions = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostReaction> postReactions = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotRecommended> notRecommendeds = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecentlyViewed> recentlyVieweds = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Noti> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatParticipant> chatParticipants = new ArrayList<>();

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateProfileUrl(String newProfileUrl) {
        this.profileUrl = newProfileUrl;
    }
}
