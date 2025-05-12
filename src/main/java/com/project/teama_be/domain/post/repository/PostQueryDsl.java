package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.querydsl.core.types.Predicate;

import java.util.List;

public interface PostQueryDsl {
    PostResDTO.HomePost getPostByPlaceName(List<String> query);

    // 키워드 검색
    PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByKeyword(
            String query,
            Predicate subQuery,
            Long cursor,
            int size
    );
}
