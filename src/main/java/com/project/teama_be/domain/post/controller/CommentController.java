package com.project.teama_be.domain.post.controller;

import com.project.teama_be.domain.post.dto.request.CommentReqDTO;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.service.command.CommentCommandService;
import com.project.teama_be.domain.post.service.query.CommentQueryService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.annotation.CurrentUser;
import com.project.teama_be.global.security.userdetails.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "댓글 API")
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;

    // GET 요청
    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 목록 조회 API by 김주헌",
            description = "게시글에 작성된 모든 댓글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<CommentResDTO.PageableComment<CommentResDTO.Comment>> getComments(
            @PathVariable @NotNull(message = "게시글ID는 필수 입력입니다.")
            Long postId,
            @RequestParam(defaultValue = "-1") @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            Long cursor,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "댓글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        return CustomResponse.onSuccess(commentQueryService.findComments(postId, cursor, size));
    }

    // 대댓글 목록 조회
    @GetMapping("/comments/{commentId}/reply")
    @Operation(
            summary = "대댓글 목록 조회 API by 김주헌",
            description = "댓글의 댓글 (대댓글)을 조회합니다. " +
                    "커서 기반 페이지네이션, 오래된 순으로 정렬합니다."
    )
    public CustomResponse<CommentResDTO.PageableComment<CommentResDTO.Reply>> getReplies(
            @PathVariable @NotNull(message = "댓글ID는 필수 입력입니다.")
            Long commentId,
            @RequestParam(defaultValue = "-1") @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            Long cursor,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "대댓글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        return CustomResponse.onSuccess(commentQueryService.findReplyComments(commentId, cursor, size));
    }

    // 내가 작성한 댓글 조회
    @GetMapping("/members/{memberId}/comments")
    @Operation(
            summary = "내가 작성한 댓글 조회 API by 김주헌",
            description = "내가 작성한 모든 댓글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<CommentResDTO.PageableComment<CommentResDTO.SimpleComment>> getMyComments(
            @PathVariable Long memberId,
            @CurrentUser
            AuthUser user,
            @RequestParam(defaultValue = "-1") @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            Long cursor,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "댓글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        return CustomResponse.onSuccess(commentQueryService.findMyComments(memberId, user, cursor, size));
    }

    // POST 요청
    // 댓글 달기
    @PostMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 작성 API by 김주헌",
            description = "댓글을 작성합니다."
    )
    public CustomResponse<CommentResDTO.CommentUpload> uploadComment(
            @PathVariable
            Long postId,
            @CurrentUser AuthUser user,
            @RequestBody CommentReqDTO.Commenting content
    ) {
        return CustomResponse.onSuccess(
                commentCommandService.createComment(
                        postId,
                        user,
                        content.content()
                )
        );
    }

    // 대댓글 작성
    @PostMapping("/comments/{commentId}/replies")
    @Operation(
            summary = "대댓글 작성 API by 김주헌",
            description = "대댓글을 작성합니다."
    )
    public CustomResponse<CommentResDTO.CommentUpload> uploadReply(
            @PathVariable Long commentId,
            @CurrentUser AuthUser user,
            @RequestBody CommentReqDTO.Commenting commentContent
    ) {
        return CustomResponse.onSuccess(
                commentCommandService.createReply(
                        commentId,
                        user,
                        commentContent.content()
                )
        );
    }

    // 댓글 좋아요
    @PostMapping("/comments/{commentId}/like")
    @Operation(
            summary = "댓글 좋아요 API by 김주헌",
            description = "댓글에 좋아요를 표시합니다."
    )
    public CustomResponse<CommentResDTO.CommentLike> likeComment(
            @PathVariable Long commentId,
            @CurrentUser AuthUser user
    ) {
        return CustomResponse.onSuccess(commentCommandService.likeComment(commentId, user));
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
