package com.project.teama_be.domain.post.dto.response;

import com.project.teama_be.domain.post.enums.ReactionType;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PostResDTO {

    // 전체 게시글
    @Builder
    public record FullPost(
            String nickname,
            String profileUrl,
            List<String> postImageUrl,
            Long imageTotalCount,
            Long postId,
            Long placeId,
            Long likeCount,
            Long commentCount,
            String placeName,
            String content,
            List<String> tags
    ) {}

    // 간소화된 게시글 (이미지만 보이는 경우)
    @Builder
    public record SimplePost(
            String postImageUrl,
            Long postId,
            String placeName,
            Long placeId
    ) {}

    // 최근 본 게시글
    @Builder
    public record RecentPost(
            String PostImageUrl,
            Long PostId,
            String placeName,
            Long placeId,
            LocalDateTime viewedAt
    ) {}

    // 홈화면에 표시할 게시글
    @Builder
    public record HomePost(
            List<SimplePost> simplePost
    ) {}

    // 커서 기반 페이지네이션 틀
    @Builder
    public record PageablePost<T>(
            List<T> post,
            Boolean hasNext,
            int pageSize,
            Long cursor
    ) {}

    // 게시글 업로드 완료
    @Builder
    public record PostUpload(
            Long postId,
            Long placeId,
            LocalDateTime createdAt
    ) {}

    // 게시글 좋아요 완료
    @Builder
    public record PostLike(
            Long postId,
            ReactionType reactionType,
            LocalDateTime updatedAt
    ) {}

    // 게시글 수정 완료
    @Builder
    public record PostUpdate(
            Long postId,
            Long placeId,
            LocalDateTime updatedAt
    ) {}

    // 게시글 삭제 완료
    @Builder
    public record PostDelete(
            Long postId,
            LocalDateTime deletedAt
    ) {}
}
