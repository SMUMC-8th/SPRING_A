package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
