package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.dto.response.PostResDTO;

import java.util.List;

public interface PostQueryDsl {
    PostResDTO.HomePost getPostByPlaceName(List<String> query);
}
