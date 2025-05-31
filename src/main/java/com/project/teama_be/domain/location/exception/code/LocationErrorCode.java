package com.project.teama_be.domain.location.exception.code;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LocationErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "LOCATION404_0",
            "위치를 찾을 수 없습니다."),
    NOT_VALID(HttpStatus.BAD_REQUEST,
            "LOCATION400_0",
            "요청한 위치 정보가 올바르지 않습니다."),;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
