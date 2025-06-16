package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.location.repository.LocationRepository;
import com.project.teama_be.domain.member.entity.QNotRecommended;
import com.project.teama_be.domain.member.entity.QRecentlyViewed;
import com.project.teama_be.domain.post.converter.PostConverter;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.QPost;
import com.project.teama_be.domain.post.entity.QPostReaction;
import com.project.teama_be.domain.post.entity.QPostTag;
import com.project.teama_be.domain.post.enums.ReactionType;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final LocationRepository locationRepository;

    // 각 가게 최신 게시글 조회 ✅
    public PostResDTO.HomePost getPost(
            AuthUser user,
            List<String> dto
    ) {

        // 조회할 객체 선언
        QPost post = QPost.post;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;
        BooleanBuilder builder = new BooleanBuilder();

        // 조건 부여
        builder.and(post.location.placeName.in(dto))
                // 내가 차단한 사용자의 게시글 제외
                .and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 게시글 제외
                .and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

        return postRepository.getPostByPlaceName(builder, dto);

    }

    // 메인화면에서 사용자 위치 중심 게시글 리턴
    public PostResDTO.HomePost getNearbyPosts(double lat, double lng, double radiusKm, AuthUser user) {
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;

        List<Location> locations = locationRepository.findByLatitudeBetweenAndLongitudeBetween(
                BigDecimal.valueOf(minLat),
                BigDecimal.valueOf(maxLat),
                BigDecimal.valueOf(minLng),
                BigDecimal.valueOf(maxLng)
        );

        List<PostResDTO.SimplePost> simplePosts = locations.stream()
                .map(loc -> postRepository.findTopByLocationOrderByCreatedAtDesc(loc))
                .filter(Objects::nonNull)
                .map(PostConverter::toSimplePost)
                .toList();

        return PostResDTO.HomePost.builder()
                .simplePost(simplePosts)
                .build();
    }

    // 키워드 검색 ✅
    public PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByKeyword(
            String query,
            String type,
            String cursor,
            int size,
            AuthUser user
    ) {

        // 동적 쿼리 : 검색 타입에 따라 조건을 추가
        BooleanBuilder builder = new BooleanBuilder();
        QPostTag postTag = QPostTag.postTag;
        QPost post = QPost.post;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        // 커서가 존재하면 이전에 조회한 게시글부터 조회
        if (!cursor.equals("-1")){
            try {
                builder.and(post.id.loe(Long.parseLong(cursor)));
            } catch (NumberFormatException e){
                throw new PostException(PostErrorCode.NOT_VALID_CURSOR);
            }
        }

        // 내가 차단한 유저 게시글 제외
        builder.and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 게시글 제외
                .and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

        switch (type.toLowerCase()) {

            // 태그 검색
            case "tag" -> builder.and(postTag.tag.tagName.likeIgnoreCase(query + "%"));

            // 가게명 검색
            case "place" -> builder.and(post.location.placeName.likeIgnoreCase(query + "%"));

            // 지역 검색
            case "address" -> builder.and(post.location.addressName.likeIgnoreCase(query + "%")
                            .or(post.location.roadAddressName.likeIgnoreCase(query + "%")));

            // 타입이 잘못된 경우
            default -> throw new PostException(PostErrorCode.NOT_VALID_TYPE);
        }

        log.info("[ 키워드 검색 ] subQuery:{}", builder);
        return postRepository.getPostsByKeyword(query, builder, size);
    }

    // 가게 게시글 모두 조회 ✅
    public PostResDTO.PageablePost<PostResDTO.FullPost> getPostsByPlaceId(
            Long placeId,
            String cursor,
            int size,
            AuthUser user
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        QPost post = QPost.post;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        builder.and(post.location.id.eq(placeId));
        if (!cursor.equals("-1")) {
            try {
                builder.and(post.id.loe(Long.parseLong(cursor)));
            } catch (NumberFormatException e){
                throw new PostException(PostErrorCode.NOT_VALID_CURSOR);
            }
        }

        // 내가 차단한 사용자의 게시글 제외
        builder.and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 게시글 제외
                .and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

        log.info("[ 가게 게시글 모두 조회 ] subQuery:{}", builder);
        return postRepository.getPostsByPlaceId(builder, size);
    }

    // 내가 작성한 게시글 조회 (마이페이지) ✅
    public PostResDTO.PageablePost<PostResDTO.SimplePost> getMyPosts(
            AuthUser user,
            String cursor,
            int size
    ) {

        BooleanBuilder builder = new BooleanBuilder();
        QPost post = QPost.post;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        builder.and(post.member.id.eq(user.getUserId()));
        if (!cursor.equals("-1")) {
            try {
                builder.and(post.id.loe(Long.parseLong(cursor)));
            } catch (NumberFormatException e){
                throw new PostException(PostErrorCode.NOT_VALID_CURSOR);
            }
        }

        // 내가 차단한 사용자의 게시글 제외
        builder.and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 게시글 제외
                .and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

        log.info("[ 내가 작성한 게시글 조회 ] subQuery:{}", builder);
        return postRepository.getMyPosts(builder, size);
    }

    // 내가 좋아요 누른 게시글 조회 ✅
    public PostResDTO.PageablePost<PostResDTO.SimplePost> getMyLikePost(
            AuthUser user,
            String cursor,
            int size
    ){

        BooleanBuilder builder = new BooleanBuilder();
        QPost post = QPost.post;
        QPostReaction postReaction = QPostReaction.postReaction;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        builder.and(postReaction.member.id.eq(user.getUserId()))
                .and(postReaction.reactionType.eq(ReactionType.LIKE));

        // 내가 차단한 사용자의 게시글 제외
        builder.and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 게시글 제외
                .and(post.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

        if (!cursor.equals("-1")) {
            try {
                builder.and(post.id.loe(Long.parseLong(cursor)));
            } catch (NumberFormatException e){
                throw new PostException(PostErrorCode.NOT_VALID_CURSOR);
            }
        }

        log.info("[ 내가 좋아요 누른 게시글 조회 ] subQuery:{}", builder);
        return postRepository.getMyLikePost(builder, size);
    }

    // 최근 본 게시글 조회
    public PostResDTO.PageablePost<PostResDTO.RecentPost> getRecentlyViewedPost(
            AuthUser user,
            String cursor,
            int size
    ){

        BooleanBuilder builder = new BooleanBuilder();
        QRecentlyViewed recentlyViewed = QRecentlyViewed.recentlyViewed;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        builder.and(recentlyViewed.member.id.eq(user.getUserId()));

        // 내가 차단한 사용자의 게시글 제외
        builder.and(recentlyViewed.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 게시글 제외
                .and(recentlyViewed.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

        if (!cursor.equals("-1")) {
            try {
                LocalDateTime newCursor = LocalDateTime.parse(cursor);
                builder.and(recentlyViewed.viewedAt.loe(newCursor));
            } catch (DateTimeParseException e) {
                throw new PostException(PostErrorCode.NOT_VALID_CURSOR);
            }
        }

        return postRepository.getRecentlyViewedPost(builder, size);
    }
}
