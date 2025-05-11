package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    PostReaction findByMemberIdAndPostId(Long memberId, Long postId);
}
