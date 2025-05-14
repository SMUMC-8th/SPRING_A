package com.project.teama_be.domain.post.exception.code;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "COMMENT404",
            "존재하지 않는 댓글입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
