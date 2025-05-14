package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.Post;

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
}
