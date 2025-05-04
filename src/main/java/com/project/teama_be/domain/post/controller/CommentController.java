package com.project.teama_be.domain.post.controller;

import com.project.teama_be.domain.post.dto.request.CommentReqDTO;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "댓글 API")
public class CommentController {

    // GET 요청
    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 목록 조회",
            description = "게시글에 작성된 모든 댓글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<CommentResDTO.PageableComment<CommentResDTO.Comment>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 대댓글 목록 조회
    @GetMapping("/comments/{commentId}/reply")
    @Operation(
            summary = "대댓글 목록 조회",
            description = "댓글의 댓글 (대댓글)을 조회합니다. " +
                    "커서 기반 페이지네이션, 오래된 순으로 정렬합니다."
    )
    public CustomResponse<CommentResDTO.PageableComment<CommentResDTO.Reply>> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 내가 작성한 댓글 조회
    @GetMapping("/members/{memberId}/comments")
    @Operation(
            summary = "내가 작성한 댓글 조회",
            description = "내가 작성한 모든 댓글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<CommentResDTO.PageableComment<CommentResDTO.SimpleComment>> getMyComments(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // POST 요청
    // 댓글 달기
    @PostMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 작성",
            description = "댓글을 작성합니다."
    )
    public CustomResponse<CommentResDTO.CommentUpload> uploadComment(
            @PathVariable Long postId,
            @RequestBody CommentReqDTO.Commenting commentContent
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 대댓글 작성
    @PostMapping("/comments/{commentId}/replies")
    @Operation(
            summary = "대댓글 작성",
            description = "대댓글을 작성합니다."
    )
    public CustomResponse<CommentResDTO.CommentUpload> uploadReply(
            @PathVariable Long commentId,
            @RequestBody CommentReqDTO.Commenting commentContent
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 댓글 좋아요
    @PostMapping("/comments/{commentId}/like")
    @Operation(
            summary = "댓글 좋아요",
            description = "댓글에 좋아요를 표시합니다."
    )
    public CustomResponse<CommentResDTO.CommentLike> likeComment(
            @PathVariable Long commentId
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 댓글 수정 (미정 기능)
    @PatchMapping("/comments/{commentId}")
    @Operation(
            summary = "댓글 수정 (미정 기능)",
            description = "댓글을 수정합니다."
    )
    public CustomResponse<CommentResDTO.CommentUpdate> commentUpdate(
            @PathVariable Long commentId,
            @RequestBody CommentReqDTO.CommentUpdate commentContent
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 댓글 삭제 (미정 기능)
    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "댓글 삭제 (미정 기능)",
            description = "댓글을 삭제합니다. (Soft Delete)"
    )
    public CustomResponse<CommentResDTO.CommentDelete> deleteComment(
            @PathVariable Long commentId
    ) {
        return CustomResponse.onSuccess(null);
    }
}
