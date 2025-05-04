package com.project.teama_be.domain.post.dto.request;

public class CommentReqDTO {

    public record Commenting(
            String content
    ) {}

    public record CommentUpdate(
            String content
    ) {}
}
