package com.project.teama_be.domain.post.repository;


import com.project.teama_be.domain.post.converter.PostConverter;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.QPost;
import com.project.teama_be.domain.post.entity.QPostImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostQueryDslImpl implements PostQueryDsl{

    private final JPAQueryFactory jpaQueryFactory;

    // 가게명으로 게시글 조회
    @Override
    public PostResDTO.HomePost getPostByPlaceName(List<String> query) {

        // 조회할 객체 선언
        QPost post = QPost.post;
        QPostImage postImage = QPostImage.postImage;

        List<PostResDTO.SimplePost> posts = new ArrayList<>();

        // 가게명이 일치하는 가게의 최신 게시글 조회 : 최적화 필요 O(3N)
        for (String placeName : query) {
            // 쿼리의 가게 최신 게시글 조회
            Post result = jpaQueryFactory
                    .selectFrom(post)
                    .where(post.location.placeName.eq(placeName))
                    .orderBy(post.id.desc())
                    .fetchFirst();
            if (result == null) {
                posts.add(PostConverter.of(null,null,placeName,null));
                continue;
            }
            // 해당 게시글 사진 조회
            String imageUrl = jpaQueryFactory
                    .select(postImage.imageUrl)
                    .from(postImage)
                    .where(postImage.post.eq(result))
                    .orderBy(postImage.id.desc())
                    .fetchFirst();
            // SimplePost로 변환
            posts.add(
                    PostConverter.of(
                            imageUrl,
                            result.getId(),
                            result.getLocation().getPlaceName(),
                            result.getLocation().getId()
                    )
            );
        }
        return PostConverter.of(posts);
    }
}
