package com.project.teama_be.domain.member.exceptioin;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_0", "해당 사용자를 찾을 수 없습니다."),
    TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER404_1", "추천하지 않을 사용자를 찾을 수 없습니다."),
    OAUTH_USER_INFO_FAIL(HttpStatus.NOT_FOUND, "MEMBER404_2", "사용자 정보 조회 실패"),
    OAUTH_TOKEN_FAIL(HttpStatus.BAD_REQUEST, "MEMBER400_1", "토큰 변환 실패"),
    OAUTH_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER400_2", "이메일 정보를 찾을 수 없습니다."),
    LOGIN_ID_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER400_3", "해당 LoginID가 이미 존재합니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER400_4", "해당 Nickname이 이미 존재합니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "MEMBER400_5", "프로필 사진 업로드 실패"),
    OAUTH_LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "MEMBER401_1", "로그인에 실패하였습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
