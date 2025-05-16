package com.project.teama_be.domain.post.repository;


import com.project.teama_be.domain.post.converter.PostConverter;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.*;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostQueryDslImpl implements PostQueryDsl{

    private final JPAQueryFactory jpaQueryFactory;

    // 각 가게 최신 게시글 조회
    @Override
    public PostResDTO.HomePost getPostByPlaceName(
            Predicate subQuery,
            List<String> query
    ) {

        // 조회할 객체 선언
        QPost post = QPost.post;
        QPostImage postImage = QPostImage.postImage;

        // 조건에 맞는 게시글 모두 조회
        List<PostResDTO.SimplePost> posts = jpaQueryFactory
                .from(post)
                .leftJoin(postImage).on(postImage.post.id.eq(post.id))
                .where(subQuery)
                .orderBy(post.id.desc())
                .transform(GroupBy.groupBy(post.location.id).list(
                        Projections.constructor(
                                PostResDTO.SimplePost.class,
                                postImage.imageUrl,
                                post.id,
                                post.location.placeName,
                                post.location.id
                        )
                ));

        // 가게의 게시글이 없거나 해당 가게가 없는 경우
        List<PostResDTO.SimplePost> result = new ArrayList<>();
        for (String eachQuery : query) {
            boolean isExist = false;
            for (PostResDTO.SimplePost eachPost : posts) {
                if (eachPost.placeName().equals(eachQuery)) {
                    result.add(eachPost);
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                PostResDTO.SimplePost emptyPost = PostConverter.toSimplePost(
                        null,
                        null,
                        eachQuery,
                        null
                );
                result.add(emptyPost);
            }
        }

        log.info("[ 게시글 조회 ] posts:{}", result);
        return PostConverter.toHomePost(result);
    }

    // 키워드 검색 ✅
    @Override
    public PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByKeyword(
            String query,
            Predicate subQuery,
            int size
    ) {
        // 조회할 객체 선언
        QPost post = QPost.post;
        QPostTag postTag = QPostTag.postTag;

        // 조건에 맞는 게시글 모두 조회
        List<Post> postList = jpaQueryFactory
                .selectFrom(post)
                .leftJoin(postTag).on(postTag.post.id.eq(post.id))
                .where(subQuery)
                .orderBy(post.id.desc())
                .fetch();

        // 결과가 존재하지 않을때
        if (postList.isEmpty()) {
            throw new PostException(PostErrorCode.NOT_FOUND_KEYWORD);
        }

        return findFullPostAttribute(postList, size);
    }

    // 내가 작성한 게시글 조회 ✅
    @Override
    public PostResDTO.PageablePost<PostResDTO.SimplePost> getMyPosts(
            Predicate subQuery,
            int size
    ) {
        // 조회할 객체 선언
        QPost post = QPost.post;

        // 조건에 맞는 게시글 모두 조회
        List<Post> postList = jpaQueryFactory
                .selectFrom(post)
                .where(subQuery)
                .orderBy(post.id.desc())
                .fetch();

        // 결과가 존재하지 않을때
        if (postList.isEmpty()) {
            throw new PostException(PostErrorCode.NOT_FOUND);
        }

        return findSimplePostAttribute(postList, size);
    }

    // 내가 좋아요 누른 게시글 조회 ✅
    public PostResDTO.PageablePost<PostResDTO.SimplePost> getMyLikePost(
            Predicate subQuery,
            int size
    ){
        // 조회할 객체 선언
        QPost post = QPost.post;
        QPostReaction postReaction = QPostReaction.postReaction;

        // 조건에 맞는 게시글 모두 조회
        List<Post> postList = jpaQueryFactory
                .selectFrom(post)
                .leftJoin(postReaction).on(postReaction.post.id.eq(post.id))
                .where(subQuery)
                .orderBy(post.id.desc())
                .fetch();

        // 결과가 존재하지 않을때
        if (postList.isEmpty()) {
            throw new PostException(PostErrorCode.NOT_FOUND);
        }

        return findSimplePostAttribute(postList, size);
    }

    // 가게 게시글 모두 조회 ✅
    @Override
    public PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByPlaceId(
            Long placeId,
            Predicate subQuery,
            int size
    ) {
        // 조회할 객체 선언
        QPost post = QPost.post;

        // 조건에 맞는 게시글 모두 조회
        List<Post> postList = jpaQueryFactory
                .selectFrom(post)
                .where(subQuery)
                .orderBy(post.id.desc())
                .fetch();

        // 결과가 존재하지 않을때
        if (postList.isEmpty()) {
            throw new PostException(PostErrorCode.NOT_FOUND);
        }
        return findFullPostAttribute(postList, size);
    }

    // SimplePost 부가 속성들 조회✅
    private PostResDTO.PageablePost<PostResDTO.SimplePost> findSimplePostAttribute(
            List<Post> postList,
            int size
    ){
        // 조회할 객체 선언
        QPostImage postImage = QPostImage.postImage;

        // 커서 지정
        Boolean hasNext = postList.size() > size;
        int pageSize = Math.min(postList.size(), size);
        Long nextCursor = postList.size() > size ?
                postList.get(pageSize).getId() : postList.get(pageSize-1).getId();

        // 게시글 size 조절
        postList = postList.subList(0, pageSize);

        // 게시글 ID 목록
        List<Long> postIdList = postList.stream()
                .map(Post::getId)
                .toList();

        // 게시글 사진 조회
        Map<Long, String> postImageList = jpaQueryFactory
                .from(postImage)
                .where(postImage.post.id.in(postIdList))
                .transform(
                        GroupBy.groupBy(postImage.post.id).as(postImage.imageUrl)
                );

        // 합치기
        List<PostResDTO.SimplePost> result = postList.stream()
                .map(eachPost ->
                        PostConverter.toSimplePost(
                                postImageList.getOrDefault(eachPost.getId(), null),
                                eachPost.getId(),
                                eachPost.getLocation().getPlaceName(),
                                eachPost.getLocation().getId()
                        )
                )
                .toList();

        log.info("[ 게시글 페이지네이션 ] result:{}, hasNext:{}, pageSize:{}, nextCursor:{}",
                result, hasNext, pageSize, nextCursor);
        return PostConverter.toPageablePost(result, hasNext, pageSize, nextCursor);
    }

    // FullPost 부가 속성들 조회 ✅
    private PostResDTO.PageablePost<PostResDTO.FullPost> findFullPostAttribute(
            List<Post> postList,
            int size
    ){

        // 조회할 객체 선언
        QPostImage postImage = QPostImage.postImage;
        QComment comment = QComment.comment;
        QPostTag postTag = QPostTag.postTag;

        // 커서 지정
        Boolean hasNext = postList.size() > size;
        int pageSize = Math.min(postList.size(), size);
        Long nextCursor = postList.size() > size ?
                postList.get(pageSize).getId() : postList.get(pageSize-1).getId();

        // 게시글 size 조절
        postList = postList.subList(0, pageSize);

        // 게시글 ID 목록
        List<Long> postIdList = postList.stream()
                .map(Post::getId)
                .toList();
        // 게시글 사진 조회
        Map<Long, List<String>> postImageList = jpaQueryFactory
                .from(postImage)
                .where(postImage.post.id.in(postIdList))
                .transform(
                        GroupBy.groupBy(postImage.post.id).as(
                                GroupBy.list(postImage.imageUrl)
                        )
                );

        // 게시글 태그 조회
        Map<Long, List<String>> postTagList = jpaQueryFactory
                .from(postTag)
                .where(postTag.post.id.in(postIdList))
                .transform(
                        GroupBy.groupBy(postTag.post.id).as(
                                GroupBy.list(postTag.tag.tagName)
                        )
                );
        // 게시글 댓글 조회
        Map<Long, Long> commentList = jpaQueryFactory
                .from(comment)
                .where(comment.post.id.in(postIdList))
                .transform(
                        GroupBy.groupBy(comment.post.id).as(
                                comment.count()
                        )
                );
        // 합치기
        List<PostResDTO.FullPost> result = postList.stream()
                .map(eachPost ->
                        PostConverter.toFullPost(
                                eachPost,
                                eachPost.getMember(),
                                postImageList.getOrDefault(eachPost.getId(), Collections.emptyList()),
                                postTagList.getOrDefault(eachPost.getId(), Collections.emptyList()),
                                commentList.getOrDefault(eachPost.getId(), 0L)
                        )
                )
                .toList();

        log.info("[ 게시글 페이지네이션 ] result:{}, hasNext:{}, pageSize:{}, nextCursor:{}",
                result, hasNext, pageSize, nextCursor);
        return PostConverter.toPageablePost(result, hasNext, pageSize, nextCursor);
    }
}
