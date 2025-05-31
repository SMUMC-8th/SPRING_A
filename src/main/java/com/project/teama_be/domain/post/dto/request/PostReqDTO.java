package com.project.teama_be.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class PostReqDTO {

    // 게사글 업로드 (위치정보, 태그, 내용)
    public record PostUpload(

            @NotNull(message = "위치정보는 필수 입력값입니다.")
            BigDecimal latitude,

            @NotNull(message = "위치정보는 필수 입력값입니다.")
            BigDecimal longitude,

            @NotNull(message = "장소명은 필수 입력값입니다.")
            String placeName,

            @NotNull(message = "지번은 필수 입력값입니다.")
            String addressName,

            @NotNull(message = "도로명은 필수 입력값입니다.")
            String roadAddressName,

            @Size(max = 5, message = "태그는 최대 5개까지 입력할 수 있습니다.")
            List<String> tags,

            String content
    ) {}

    // 게시글 수정
    public record PostUpdate(
            String content,
            @Size(max = 5, message = "태그는 최대 5개까지 입력할 수 있습니다.")
            List<String> tags,
            Long placeId
    ) {}

    // 각 가게 최신 게시글 조회
    public record Query(
            @NotBlank(message = "쿼리는 필수 입력값입니다.")
            String query
    ) {}
}
