package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.entity.RecentlyViewed;
import com.project.teama_be.domain.post.dto.request.PostReqDTO;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostImage;
import com.project.teama_be.domain.post.entity.PostReaction;
import com.project.teama_be.domain.post.enums.ReactionType;

import java.time.LocalDateTime;
import java.util.List;

public class PostConverter {

    // 게시글 업로드 : Location, Member, ReqDTO -> Post
    public static Post toPost(
            Location location,
            Member member,
            PostReqDTO.PostUpload postUpload
    ){
        return Post.builder()
                .location(location)
                .member(member)
                .content(postUpload.content())
                .build();
    }

    // 게시글 이미지 저장 : Post, Url -> PostImage
    public static PostImage toPostImage(Post post, String url){
        return PostImage.builder()
                .post(post)
                .imageUrl(url)
                .build();
    }

    // 게시글 업로드 완료 : Post -> ResDTO
    public static PostResDTO.PostUpload toPostUpload(Post post){
        return PostResDTO.PostUpload.builder()
                .postId(post.getId())
                .placeId(post.getLocation().getId())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 게시글 좋아요 : User, Post, ReactionType -> PostReaction
    public static PostReaction toPostReaction(
            Member member,
            Post post,
            ReactionType reactionType
    ){
        return PostReaction.builder()
                .member(member)
                .post(post)
                .reactionType(reactionType)
                .build();
    }

    // 최근 본 게시글 추가: Post, Member -> RecentlyViewed
    public static RecentlyViewed toRecentlyViewed(
            Post post,
            Member member
    ){
        return RecentlyViewed.builder()
                .post(post)
                .member(member)
                .viewedAt(LocalDateTime.now())
                .build();
    }

    // 게시글 좋아요 완료 : User, Post, ReactionType -> ResDTO
    public static PostResDTO.PostLike toPostLike(
            PostReaction postReaction
    ){
        return PostResDTO.PostLike.builder()
                .postId(postReaction.getPost().getId())
                .reactionType(postReaction.getReactionType())
                .updatedAt(postReaction.getUpdatedAt())
                .build();
    }

    // postImageUrl, postId, placeName, placeId -> SimplePost
    public static PostResDTO.SimplePost toSimplePost(
            String postImageUrl,
            Long postId,
            String placeName,
            Long placeId
    ){
        return PostResDTO.SimplePost.builder()
                .postImageUrl(postImageUrl)
                .postId(postId)
                .placeName(placeName)
                .placeId(placeId)
                .build();
    }

    // Info -> FullPost
    public static PostResDTO.FullPost toFullPost(
            Post post,
            Member member,
            List<String> imageList,
            List<String> tagList,
            Long commentCount
    ){
        return PostResDTO.FullPost.builder()
                .nickname(member.getNickname())
                .profileUrl(member.getProfileUrl())
                .postImageUrl(imageList)
                .imageTotalCount((long) imageList.size())
                .postId(post.getId())
                .placeId(post.getLocation().getId())
                .likeCount(post.getLikeCount())
                .commentCount(commentCount)
                .placeName(post.getLocation().getPlaceName())
                .content(post.getContent())
                .tags(tagList)
                .build();
    }

    // 홈화면용 게시글 조회: List<SimplePost> -> HomePost
    public static PostResDTO.HomePost toHomePost(
            List<PostResDTO.SimplePost> posts
    ){
        return PostResDTO.HomePost.builder()
                .simplePost(posts)
                .build();
    }

    // 홈화면용 위치기반 게시글 조회
    public static PostResDTO.SimplePost toSimplePost(Post post) {
        String imageUrl = post.getPostImages().isEmpty()
                ? null
                : post.getPostImages().get(0).getImageUrl();

        return PostResDTO.SimplePost.builder()
                .postId(post.getId())
                .postImageUrl(imageUrl)
                .placeId(post.getLocation().getId())
                .placeName(post.getLocation().getPlaceName())
                .build();
    }

    // 커서 기반 게시글 조회 : List<T> -> PageablePost
    public static <T> PostResDTO.PageablePost<T> toPageablePost(
            List<T> posts,
            PostResDTO.Cursor cursor
    ){
        return PostResDTO.PageablePost.<T>builder()
                .post(posts)
                .cursor(cursor.nextCursor())
                .hasNext(cursor.hasNext())
                .pageSize(cursor.pageSize())
                .build();
    }

    // 최근 본 게시글 조회
    public static PostResDTO.RecentPost toRecentlyViewedPost(
            PostResDTO.SimplePost simplePost,
            LocalDateTime viewedAt
    ){
        return PostResDTO.RecentPost.builder()
                .PostId(simplePost.postId())
                .PostImageUrl(simplePost.postImageUrl())
                .placeName(simplePost.placeName())
                .placeId(simplePost.placeId())
                .viewedAt(viewedAt)
                .build();
    }

    // 게시글 수정
    public static PostResDTO.PostUpdate toPostUpdate(
            Post post
    ){
        return PostResDTO.PostUpdate.builder()
                .postId(post.getId())
                .placeId(post.getLocation().getId())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // 게시글 삭제
    public static PostResDTO.PostDelete toPostDelete(
            Post post,
            LocalDateTime deletedAt
    ){
        return PostResDTO.PostDelete.builder()
                .postId(post.getId())
                .deletedAt(deletedAt)
                .build();
    }

    // 커서 포장
    public static PostResDTO.Cursor toCursor(
            String cursor,
            Boolean hasNext,
            int pageSize
    ){
        return PostResDTO.Cursor.builder()
                .nextCursor(cursor)
                .hasNext(hasNext)
                .pageSize(pageSize)
                .build();
    }
}
