package com.project.teama_be.domain.post.controller;


import com.project.teama_be.domain.post.dto.request.PostReqDTO;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.service.command.PostCommandService;
import com.project.teama_be.domain.post.service.query.PostQueryService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.annotation.CurrentUser;
import com.project.teama_be.global.security.userdetails.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "게시글 API")
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    // GET 요청
    // 각 가게 최신 게시글 조회 ✅
    @GetMapping("/places/posts")
    @Operation(
            summary = "각 가게 최신 게시글 조회 API by 김주헌",
            description = "해당 가게의 게시글 중 최근 게시된 게시글을 조회합니다." +
                    "Query Parameter를 중복하여 사용함으로써 홈화면(지도 화면)에 표시할 게시글을 조회할 수 있습니다." +
                    "각 가게의 최신 게시글 하나만 조회합니다."
    )
    public CustomResponse<PostResDTO.HomePost> getPostsByPlaceName(
            @RequestParam @Size(min = 1, message = "검색할 가게가 최소 하나 이상 있어야 합니다.")
            List<String> query
    ) {

        log.info("[ 각 가게 최신 게시글 조회 ] queryCnt:{}", query.size());
        return CustomResponse.onSuccess(postQueryService.getPost(query));
    }

    // 키워드 검색 ✅
    @GetMapping("/posts")
    @Operation(
            summary = "키워드 검색 API by 김주헌",
            description = "키워드를 통해 게시글을 조회합니다. " +
                    "키워드 종류를 선택해야 합니다. (tag, place, address)" +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.FullPost>> getPostsByKeyword(
            @RequestParam @NotBlank(message = "키워드가 비어있으면 안됩니다.")
            String query,
            @RequestParam @NotBlank(message = "키워드 종류가 비어있으면 안됩니다.")
            String type,
            @RequestParam(defaultValue = "-1") @NotNull(message = "커서의 기본값은 -1입니다.")
            @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            String cursor,
            @RequestParam(defaultValue = "1") @NotNull(message = "조회할 데이터 사이즈를 요청해야 합니다.")
            @Min(value = 1, message = "게시글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        log.info("[ 키워드 검색 ] query:{}, type:{}, cursor:{}, size:{}", query, type, cursor, size);
        return CustomResponse.onSuccess(postQueryService.getPostsByKeyword(query, type, cursor, size));
    }

    // 가게 게시글 모두 조회 ✅
    @GetMapping("/places/{placeId}/posts")
    @Operation(
            summary = "가게 게시글 모두 조회 API by 김주헌",
            description = "해당 가게의 모든 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.FullPost>> getAllPostsAboutPlace(
            @PathVariable @NotNull(message = "장소ID는 필수로 적어야합니다.")
            @Min(value = 0, message = "장소ID는 최소 1부터 시작합니다.")
            Long placeId,
            @RequestParam(defaultValue = "-1") @NotNull(message = "커서의 기본값은 -1입니다.")
            @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            String cursor,
            @RequestParam(defaultValue = "1") @NotNull(message = "조회할 데이터 사이즈를 요청해야 합니다.")
            @Min(value = 1, message = "게시글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        log.info("[ 가게 게시글 모두 조회 ] placeId:{}, cursor:{}, size:{}", placeId, cursor, size);
        return CustomResponse.onSuccess(postQueryService.getPostsByPlaceId(placeId, cursor, size));
    }

    // 내가 작성한 게시글 조회 (마이페이지) ✅
    @GetMapping("/me/posts")
    @Operation(
            summary = "내가 작성한 게시글 조회 (마이페이지) API by 김주헌",
            description = "마이페이지에서 내가 올렸던 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.SimplePost>> getMyPosts(
            @CurrentUser
            AuthUser user,
            @RequestParam(defaultValue = "-1") @NotNull(message = "커서의 기본값은 -1입니다.")
            @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            String cursor,
            @RequestParam(defaultValue = "1") @NotNull(message = "조회할 데이터 사이즈를 요청해야 합니다.")
            @Min(value = 1, message = "게시글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        log.info("[ 내가 작성한 게시글 조회 ] user:{}, cursor:{}, size:{}",
                user.getLoginId(), cursor, size);
        return CustomResponse.onSuccess(postQueryService.getMyPosts(user, cursor, size));
    }

    // 최근 본 게시글 조회 ✅
    @GetMapping("/me/recent-view-post")
    @Operation(
            summary = "최근 본 게시글 조회 API by 김주헌",
            description = "마이페이지에서 최근 본 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.RecentPost>> getRecentViewPosts(
            @CurrentUser
            AuthUser user,
            @RequestParam(defaultValue = "-1") @NotNull(message = "커서의 기본값은 -1입니다.")
            String cursor,
            @RequestParam(defaultValue = "1") @NotNull(message = "조회할 데이터 사이즈를 요청해야 합니다.")
            @Min(value = 1, message = "게시글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        log.info("[ 최근 본 게시글 조회 ] cursor:{}, size:{}", cursor, size);
        return CustomResponse.onSuccess(postQueryService.getRecentlyViewedPost(user, cursor, size));
    }

    // 내가 좋아요 누른 게시글 조회 ✅
    @GetMapping("/me/like-post")
    @Operation(
            summary = "내가 좋아요 누른 게시글 조회 API by 김주헌",
            description = "마이페이지에서 좋아요를 누른 게시글을 조회합니다. " +
                    "커서 기반 페이지네이션, 최신 순으로 정렬합니다."
    )
    public CustomResponse<PostResDTO.PageablePost<PostResDTO.SimplePost>> getLikedPosts(
            @CurrentUser
            AuthUser user,
            @RequestParam(defaultValue = "-1") @NotNull(message = "커서의 기본값은 -1입니다.")
            @Min(value = -1, message = "커서는 -1 이상이어야 합니다.")
            String cursor,
            @RequestParam(defaultValue = "1") @NotNull(message = "조회할 데이터 사이즈를 요청해야 합니다.")
            @Min(value = 1, message = "게시글은 최소 하나 이상 조회해야 합니다.")
            int size
    ) {
        log.info("[ 내가 좋아요 누른 게시글 조회 ] user:{}, cursor:{}, size:{}",
                user.getLoginId(), cursor, size);
        return CustomResponse.onSuccess(postQueryService.getMyLikePost(user, cursor, size));
    }

    // POST 요청
    // 게시글 업로드 ✅ (파일은 multipart/form-data, 게시글 관련 데이터는 application/json)
    @PostMapping(
            value = "/posts",
            consumes = {
                MediaType.MULTIPART_FORM_DATA_VALUE
            }
    )
    @Operation(
            summary = "게시글 업로드 API by 김주헌",
            description = "게시글을 업로드합니다." +
                    "파일은 image/**, JSON은 application/json으로 요청해주세요."
    )
    public CustomResponse<PostResDTO.PostUpload> uploadPost(
            @CurrentUser
            AuthUser user,
            @RequestPart @NotEmpty(message = "게시글 이미지는 필수 입력값입니다.")
            List<MultipartFile> image,
            @RequestPart @Valid @NotNull(message = "게시글 내용은 필수 입력값입니다.")
            PostReqDTO.PostUpload postContent
    ) {

        log.info("[ 게시글 업로드 ] user:{}, image:{}, postContent:{}", user.getLoginId(), image.size(), postContent.content());
        return CustomResponse.onSuccess(
                postCommandService.PostUpload(
                        user,
                        image,
                        postContent
                )
        );
    }

    // 게시글 좋아요 ✅
    @PostMapping("/posts/{postId}/like")
    @Operation(
            summary = "게시글 좋아요 API by 김주헌",
            description = "게시글에 좋아요를 반영합니다."
    )
    public CustomResponse<PostResDTO.PostLike> likePost(
            @CurrentUser
            AuthUser user,
            @PathVariable @NotNull(message = "게시글ID는 필수 입력입니다.")
            Long postId
    ) {
        log.info("[ 게시글 좋아요 ] user:{}, postId:{}", user.getLoginId(), postId);
        return CustomResponse.onSuccess(postCommandService.PostLike(user, postId));
    }

    // 최근 본 게시글 추가 ✅
    @Operation(
            summary = "최근 본 게시글 추가 API by 김주헌",
            description = "최근 본 게시글을 추가합니다." +
                    "게시글을 조회할때마다 이 API를 호출해 주세요."
    )
    @PostMapping("/posts/{postId}/view")
    public CustomResponse<Void> addRecentViewPost(
            @PathVariable @NotNull(message = "게시글ID는 필수 입력입니다.")
            Long postId,
            @CurrentUser
            AuthUser user
    ) {
        log.info("[ 최근 본 게시글 추가 ] userID:{}, postId:{}", user.getUserId(), postId);
        postCommandService.addRecentPost(postId, user);
        return CustomResponse.onSuccess(null);
    }

    // PATCH 요청
    // 게시글 수정
    @PatchMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 수정 API by 김주헌",
            description = "게시글을 수정합니다."
    )
    public CustomResponse<PostResDTO.PostUpdate> updatePost(
            @PathVariable @NotNull(message = "게시글ID는 필수 입력입니다.")
            Long postId,
            @CurrentUser
            AuthUser user,
            @RequestBody(required = false) @Valid
            PostReqDTO.PostUpdate dto
    ) {
        Optional<PostResDTO.PostUpdate> result = postCommandService.PostUpdate(postId, user, dto);
        return result.map(CustomResponse::onSuccess).orElseGet(() ->
                CustomResponse.onSuccess(HttpStatus.NO_CONTENT, null));
    }

    // DELETE 요청 (SoftDelete 적용해야 함)
    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 삭제 API by 김주헌",
            description = "게시글을 삭제합니다 (SoftDelete)" +
                    "삭제한 게시글을 복구하는 API가 없습니다. 사용에 유의해주세요."
    )
    public CustomResponse<PostResDTO.PostDelete> deletePost(
            @PathVariable @NotNull(message = "게시글ID는 필수 입력입니다.")
            Long postId,
            @CurrentUser
            AuthUser user
    ) {
        return CustomResponse.onSuccess(postCommandService.deletePost(postId, user));
    }
}
