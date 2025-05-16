package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.QPost;
import com.project.teama_be.domain.post.entity.QPostReaction;
import com.project.teama_be.domain.post.entity.QPostTag;
import com.project.teama_be.domain.post.enums.ReactionType;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.project.teama_be.global.security.exception.SecurityErrorCode;
import com.project.teama_be.global.security.userdetails.AuthUser;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

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
                return postRepository.getPostsByKeyword(query, builder, size);
            }

            // 가게명 검색
            case "place" -> {
                builder.and(post.location.placeName.likeIgnoreCase(query + "%"));
                return postRepository.getPostsByKeyword(query, builder, size);
            }

            // 지역 검색
            case "address" -> {
                builder.and(post.location.addressName.likeIgnoreCase(query + "%")
                                .or(post.location.roadAddressName.likeIgnoreCase(query + "%")));
                return postRepository.getPostsByKeyword(query, builder, size);
            }

            // 타입이 잘못된 경우
            default -> throw new PostException(PostErrorCode.NOT_VALID_TYPE);
        }
    }

    // 가게 게시글 모두 조회
    public PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByPlaceId(
            Long placeId,
            Long cursor,
            int size
    ) {
        log.info("[ 가게 게시글 모두 조회 ] 가게 게시글을 모두 조회합니다.");
        BooleanBuilder builder = new BooleanBuilder();
        QPost post = QPost.post;

        builder.and(post.location.id.eq(placeId));
        if (cursor != -1) {
            builder.and(post.id.loe(cursor));
        }

        return postRepository.getPostsByPlaceId(placeId, builder, size);
    }

    // 내가 작성한 게시글 조회 (마이페이지)
    public PostResDTO.PageablePost<PostResDTO.SimplePost> getMyPosts(
            Long memberId,
            AuthUser user,
            Long cursor,
            int size
    ) {

        // 로그인 유저와 memberID가 같은지 검증
        validateMember(user, memberId);

        log.info("[ 내가 작성한 게시글 조회 ] 내가 작성한 게시글을 조회합니다.");
        BooleanBuilder builder = new BooleanBuilder();
        QPost post = QPost.post;

        builder.and(post.member.id.eq(memberId));
        if (cursor != -1) {
            builder.and(post.id.loe(cursor));
        }
        return postRepository.getMyPosts(builder, size);
    }

    // 내가 좋아요 누른 게시글 조회
    public PostResDTO.PageablePost<PostResDTO.SimplePost> getMyLikePost(
            Long memberId,
            AuthUser user,
            Long cursor,
            int size
    ){
        // 로그인 유저와 memberID가 같은지 검증
        validateMember(user, memberId);

        log.info("[ 내가 좋아요 누른 게시글 조회 ] 내가 좋아요 누른 게시글을 조회합니다.");
        BooleanBuilder builder = new BooleanBuilder();
        QPost post = QPost.post;
        QPostReaction postReaction = QPostReaction.postReaction;

        builder.and(postReaction.member.id.eq(memberId))
                .and(postReaction.reactionType.eq(ReactionType.LIKE));
        if (cursor != -1) {
            builder.and(post.id.loe(cursor));
        }
        return postRepository.getMyLikePost(builder, size);
    }

     // 로그인 유저 <-> memberID 대조
    private void validateMember(AuthUser user, Long memberId) {

        // 현재 유저와 맞는지 대조
        if (!user.getUserId().equals(memberId)) {
            throw new PostException(SecurityErrorCode.FORBIDDEN);
        }
    }
}
