package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTagName(String tagName);

    List<Tag> findByTagNameIn(Collection<String> tagNames);
}
