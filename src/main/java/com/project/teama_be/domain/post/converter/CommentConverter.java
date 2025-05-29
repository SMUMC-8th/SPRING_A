package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.CommentReaction;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.enums.ReactionType;

import java.time.LocalDateTime;
import java.util.List;

public class CommentConverter {

    // 댓글 작성
    public static Comment toComment(
            Post post,
            Member member,
            String content
    ){
      return Comment.builder()
              .post(post)
              .member(member)
              .content(content)
              .build();
    }

    // 대댓글 작성
    public static Comment toReply(
            Post post,
            Member member,
            String content,
            Long parentId
    ){
      return Comment.builder()
              .post(post)
              .member(member)
              .content(content)
              .parentId(parentId)
              .build();
    }

    // Comment -> CommentUploadDTO
    public static CommentResDTO.CommentUpload toCommentUpload(Comment comment){
        return CommentResDTO.CommentUpload.builder()
                .commentId(comment.getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // comment, member, reactionType -> CommentReaction
    public static CommentReaction toCommentReaction(
            Comment comment,
            Member member,
            ReactionType reactionType
    ){
        return CommentReaction.builder()
                .comment(comment)
                .member(member)
                .reactionType(reactionType)
                .build();
    }

    // 댓글 좋아요
    public static CommentResDTO.CommentLike toCommentLike(
            CommentReaction commentReaction
    ){
        return CommentResDTO.CommentLike.builder()
                .commentId(commentReaction.getComment().getId())
                .reactionType(commentReaction.getReactionType())
                .updatedAt(commentReaction.getCreatedAt())
                .build();
    }

    // List<T> -> PageableComment
    public static <T> CommentResDTO.PageableComment<T> toPageableComment(
            List<T> comments,
            Boolean hasNext,
            int pageSize,
            String cursor
    ){
        return CommentResDTO.PageableComment.<T>builder()
                .comment(comments)
                .hasNext(hasNext)
                .pageSize(pageSize)
                .cursor(cursor)
                .build();
    }

    // 댓글 수정
    public static CommentResDTO.CommentUpdate toCommentUpdate(
            Comment comment
    ){
        return CommentResDTO.CommentUpdate.builder()
                .commentId(comment.getId())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    // 댓글 삭제
    public static CommentResDTO.CommentDelete toCommentDelete(
            Comment comment,
            LocalDateTime deletedAt
    ){
        return CommentResDTO.CommentDelete.builder()
                .commentId(comment.getId())
                .deletedAt(deletedAt)
                .build();
    }
}
