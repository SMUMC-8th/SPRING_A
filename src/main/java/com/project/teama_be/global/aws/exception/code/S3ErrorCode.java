package com.project.teama_be.global.aws.exception.code;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

    FILE_SIZE_OVER(HttpStatus.BAD_REQUEST,
            "S3400_0",
            "파일 크기가 제한 크기를 초과하였습니다."),
    NOT_FOUND_FILE(HttpStatus.BAD_REQUEST,
            "S3400_1",
            "이미지 파일이 존재하지 않습니다."),
    NOT_IMAGE_FILE(HttpStatus.BAD_REQUEST,
            "S3400_2",
            "이미지 파일은 jpg, jpeg, png 파일만 가능합니다."),
    IO_EXCEPTION(HttpStatus.BAD_REQUEST,
            "S3400_3",
            "파일 업로드 중 오류가 발생하였습니다."),
    S3_EXCEPTION(HttpStatus.BAD_REQUEST,
            "S3400_4",
            "S3에 파일 업로드 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
