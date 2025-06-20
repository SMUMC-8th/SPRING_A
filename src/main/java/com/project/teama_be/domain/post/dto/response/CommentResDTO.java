package com.project.teama_be.domain.post.dto.response;

import com.project.teama_be.domain.post.enums.ReactionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResDTO {

    // 댓글
    @Builder
    public record Comment(
            Long commentId,
            String nickname,
            String profileUrl,
            String content,
            Long likeCount,
            Long replyCount
    ) {}

    // 대댓글
    @Builder
    public record Reply(
            Long commentId,
            String nickname,
            String profileUrl,
            String content,
            Long likeCount
    ) {}

    // 간략화 한 댓글
    @Builder
    public record SimpleComment(
            Long commentId,
            String content,
            Long likeCount
    ) {}

    // 커서 기반 페이지네이션 (댓글은 최신 순, 대댓글은 오래된 순)
    @Builder
    public record PageableComment<T>(
            List<T> comment,
            Boolean hasNext,
            int pageSize,
            String cursor
    ) {}

    // 댓글 작성
    @Builder
    public record CommentUpload(
            Long commentId,
            LocalDateTime createdAt
    ) {}

    // 댓글 좋아요
    @Builder
    public record CommentLike(
            Long commentId,
            ReactionType reactionType,
            LocalDateTime updatedAt
    ) {}

    // 댓글 수정
    @Builder
    public record CommentUpdate(
            Long commentId,
            LocalDateTime updatedAt
    ) {}

    // 댓글 삭제
    @Builder
    public record CommentDelete(
            Long commentId,
            LocalDateTime deletedAt
    ) {}
}
