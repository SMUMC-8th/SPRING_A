package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryDsl {

    Optional<Post> findPostById(Long postId);
}
