package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.location.entity.QLocation;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.QPost;
import com.project.teama_be.domain.post.entity.QPostTag;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    // 가게명으로 게시글 조회
    public PostResDTO.HomePost getPost(
            List<String> query
    ) {

        log.info("[ 게시글 조회 ] 홈화면용 게시글을 조회합니다.]");
        return postRepository.getPostByPlaceName(query);

    }

    // 키워드 검색
    public PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByKeyword(
            String query,
            String type,
            Long cursor,
            int size
    ) {

        log.info("[ 키워드 검색 ] 키워드 검색을 시작합니다.");

        // 동적 쿼리 : 검색 타입에 따라 조건을 추가
        BooleanBuilder builder = new BooleanBuilder();
        QPostTag postTag = QPostTag.postTag;
        QPost post = QPost.post;
        // 커서가 존재하면 이전에 조회한 게시글부터 조회
        if (cursor != -1){
            builder.and(post.id.loe(cursor));
        }

        switch (type.toLowerCase()) {

            // 태그 검색
            case "tag" -> {
                builder.and(postTag.tag.tagName.likeIgnoreCase(query + "%"));
                return postRepository.getPostsByKeyword(query, builder, cursor, size);
            }

            // 가게명 검색
            case "place" -> {
                builder.and(post.location.placeName.likeIgnoreCase(query + "%"));
                return postRepository.getPostsByKeyword(query, builder, cursor, size);
            }

            // 지역 검색
            case "address" -> {
                builder.and(post.location.addressName.likeIgnoreCase(query + "%")
                                .or(post.location.roadAddressName.likeIgnoreCase(query + "%")));
                return postRepository.getPostsByKeyword(query, builder, cursor, size);
            }

            // 타입이 잘못된 경우
            default -> {
                throw new PostException(PostErrorCode.NOT_VALID_TYPE);
            }
        }
    }
}
