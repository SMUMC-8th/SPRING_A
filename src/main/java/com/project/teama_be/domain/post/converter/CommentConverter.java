package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.Post;

public class CommentConverter {

    // postId, memberId, content -> Comment
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

    // Comment -> CommentUploadDTO
    public static CommentResDTO.CommentUpload toCommentUpload(Comment comment){
        return CommentResDTO.CommentUpload.builder()
                .commentId(comment.getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
