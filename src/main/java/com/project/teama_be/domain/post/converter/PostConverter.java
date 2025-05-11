package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.post.dto.request.PostReqDTO;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostImage;
import com.project.teama_be.domain.post.entity.PostReaction;
import com.project.teama_be.domain.post.enums.ReactionType;

public class PostConverter {

    // 게시글 업로드 : Location, Member, ReqDTO -> Post
    public static Post of(
            Location location,
            Member member,
            PostReqDTO.PostUpload postUpload
    ){
        return Post.builder()
                .location(location)
                .member(member)
                .content(postUpload.content())
                .isPrivate(postUpload.isPrivate())
                .build();
    }

    // 게시글 이미지 저장 : Post, Url -> PostImage
    public static PostImage of(Post post, String url){
        return PostImage.builder()
                .post(post)
                .imageUrl(url)
                .build();
    }

    // 게시글 업로드 완료 : Post -> ResDTO
    public static PostResDTO.PostUpload of(Post post){
        return PostResDTO.PostUpload.builder()
                .postId(post.getId())
                .placeId(post.getLocation().getId())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 게시글 좋아요 : User, Post, ReactionType -> PostReaction
    public static PostReaction of(
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

    // 게시글 좋아요 완료 : User, Post, ReactionType -> ResDTO
    public static PostResDTO.PostLike of(
            PostReaction postReaction
    ){
        return PostResDTO.PostLike.builder()
                .postId(postReaction.getPost().getId())
                .reactionType(postReaction.getReactionType())
                .updatedAt(postReaction.getUpdatedAt())
                .build();
    }
}
