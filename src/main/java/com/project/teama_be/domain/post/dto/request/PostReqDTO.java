package com.project.teama_be.domain.post.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class PostReqDTO {

    // 게사글 업로드 (위치정보, 태그, 내용, 비공개 여부)
    public record postUpload(
            BigDecimal latitude,
            BigDecimal longitude,
            String placeName,
            List<String> tags,
            String content,
            Boolean isPrivate
    ) {}

    // 게시글 수정
    public record postUpdate(
            Long memberId,
            String content,
            List<String> tags,
            Long placeId
    ) {}
}
