package com.project.teama_be.domain.post.controller;


import com.project.teama_be.domain.post.dto.request.PostReqDTO;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "게시글 API")
public class PostController {

    // GET 요청
    // 가게명으로 게시글 조회
    @GetMapping("/places/posts")
    @Operation(
            summary = "가게명으로 게시글 조회",
            description = "해당 가게의 게시글 중 최근 게시된 게시글을 조회합니다." +
                    "Query Parameter를 중복하여 사용함으로써 홈화면(지도 화면)에 표시할 게시글을 조회할 수 있습니다." +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.SimplePost>> getPostsByPlaceName(
            @RequestParam List<String> query,
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 키워드 검색
    @GetMapping("/posts")
    @Operation(
            summary = "키워드 검색",
            description = "키워드를 통해 게시글을 조회합니다. " +
                    "키워드 종류를 선택해야 합니다. (태그, 장소명) " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.FullPost>> getPostsByKeyword(
            @RequestParam String query,
            @RequestParam String type,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 가게 게시글 모두 조회
    @GetMapping("/places/{placeId}/posts")
    @Operation(
            summary = "가게 게시글 모두 조회",
            description = "해당 가게의 모든 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.FullPost>> getAllPostsAboutPlace(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 내가 작성한 게시글 조회 (마이페이지)
    @GetMapping("/members/{memberId}/posts")
    @Operation(
            summary = "내가 작성한 게시글 조회 (마이페이지)",
            description = "마이페이지에서 내가 올렸던 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.SimplePost>> getMyPosts(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 최근 본 게시글 조회
    @GetMapping("/members/{memberId}/posts/recent")
    @Operation(
            summary = "최근 본 게시글 조회",
            description = "마이페이지에서 최근 본 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.RecentPost>> getRecentViewPosts(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 내가 좋아요 누른 게시글 조회
    @GetMapping("/members/{memberId}/posts/like")
    @Operation(
            summary = "내가 좋아요 누른 게시글 조회",
            description = "마이페이지에서 좋아요를 누른 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.SimplePost>> getLikedPosts(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "-1") Long cursor,
            @RequestParam(defaultValue = "1") Long size
    ) {
        return CustomResponse.onSuccess(null);
    }

    // POST 요청
    // 게시글 업로드 (파일은 multipart/form-data, 게시글 관련 데이터는 application/json)
    @PostMapping(
            value = "/posts",
            consumes = {
                MediaType.MULTIPART_FORM_DATA_VALUE
            }
    )
    @Operation(
            summary = "게시글 업로드",
            description = "게시글을 업로드합니다. " +
                    "반드시 RequestBody를 multipart/form-data로 전송해 주세요."
    )
    public CustomResponse<PostResDTO.PostUpload> uploadPost(
            @RequestPart List<MultipartFile> image,
            @RequestBody PostReqDTO.postUpload postContent
    ) {
        return CustomResponse.onSuccess(null);
    }

    // 게시글 좋아요
    @PostMapping("/posts/{postId}/like")
    @Operation(
            summary = "게시글 좋아요",
            description = "게시글에 좋아요를 반영합니다."
    )
    public CustomResponse<PostResDTO.PostLike> likePost(
            @PathVariable Long postId
    ) {
        return CustomResponse.onSuccess(null);
    }

    // PATCH 요청
    // 게시글 수정 (미정 기능)
    @PatchMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 수정 (미정 기능)",
            description = "게시글을 수정합니다."
    )
    public CustomResponse<PostResDTO.PostUpdate> updatePost(
            @PathVariable Long postId,
            @RequestBody(required = false) PostReqDTO.postUpdate postContent
    ) {
        return CustomResponse.onSuccess(null);
    }

    // DELETE 요청 (SoftDelete 적용해야 함)
    // 게시글 삭제 (미정 기능)
    @DeleteMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 삭제 (미정 기능)",
            description = "게시글을 삭제합니다 (SoftDelete)"
    )
    public CustomResponse<PostResDTO.PostDelete> deletePost(
            @PathVariable Long postId
    ) {
        return CustomResponse.onSuccess(null);
    }
}
