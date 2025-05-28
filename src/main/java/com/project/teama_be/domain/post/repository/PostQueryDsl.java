package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.querydsl.core.types.Predicate;

import java.util.List;

public interface PostQueryDsl {

    // 각 가게 최신 게시글 조회
    PostResDTO.HomePost getPostByPlaceName(
            Predicate subQuery,
            List<String> query
    );

    // 키워드 검색
    PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByKeyword(
            String query,
            Predicate subQuery,
            int size
    );

    // 내가 작성한 게시글 조회
    PostResDTO.PageablePost<PostResDTO.SimplePost> getMyPosts(
            Predicate subQuery,
            int size
    );

    // 내가 좋아요 누른 게시글 조회
    PostResDTO.PageablePost<PostResDTO.SimplePost> getMyLikePost(
            Predicate subQuery,
            int size
    );

    // 가게 게시글 모두 조회
    PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByPlaceId(
            Long placeId,
            Predicate subQuery,
            int size
    );

    // 최근 본 게시글 조회
    PostResDTO.PageablePost<PostResDTO.RecentPost> getRecentlyViewedPost(
            Predicate subQuery,
            int size
    );
}
