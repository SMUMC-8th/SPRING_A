package com.project.teama_be.domain.post.exception.code;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "POST404_0",
            "해당 게시글이 존재하지 않습니다."),
    NOT_FOUND_KEYWORD(HttpStatus.NOT_FOUND,
                      "POST404_1",
            "해당 키워드의 게시글이 존재하지 않습니다."),
    NOT_VALID_TYPE(HttpStatus.BAD_REQUEST,
            "VALID400_0",
            "잘못된 키워드 종류입니다."),
    NOT_VALID_CURSOR(HttpStatus.BAD_REQUEST,
            "VALID400_1",
            "잘못된 커서값입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "USER404_1",
            "해당 유저가 존재하지 않습니다."),
    USER_NOT_MATCH(HttpStatus.BAD_REQUEST,
            "USER400_2",
            "해당 게시글을 작성한 유저만 접근 가능합니다."),
    VIEWED_POST_NOT_FOUND(HttpStatus.NOT_FOUND,
            "POST404_2",
            "해당 유저의 최근에 본 게시글이 존재하지 않습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
