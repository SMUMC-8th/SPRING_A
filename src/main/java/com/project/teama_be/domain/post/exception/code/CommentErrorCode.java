package com.project.teama_be.domain.post.exception.code;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "COMMENT404_0",
            "존재하지 않는 댓글입니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND,
            "COMMENT404_1",
            "해당 게시글에 댓글이 존재하지 않습니다."),
    NOT_FOUND_REPLY(HttpStatus.NOT_FOUND,
            "COMMENT404_2",
            "해당 댓글에 대댓글이 없습니다."),;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
