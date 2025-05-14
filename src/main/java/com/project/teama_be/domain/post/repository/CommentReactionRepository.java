package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.entity.CommentReaction;
import com.project.teama_be.domain.post.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    Optional<CommentReaction> findByCommentId(Long commentId);
}
