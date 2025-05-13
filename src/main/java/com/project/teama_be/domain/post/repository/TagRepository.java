package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByTagName(String tagName);

    Tag findByTagName(String tagName);

    List<Tag> findByTagNameIn(Collection<String> tagNames);
}
