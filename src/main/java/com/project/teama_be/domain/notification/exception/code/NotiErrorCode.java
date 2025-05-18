package com.project.teama_be.domain.notification.exception.code;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotiErrorCode implements BaseErrorCode {

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI404_1", "FCM 토큰이 없습니다."),
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "NOTI400_1", "유효하지 않은 FCM 토큰입니다."),

    NOT_APPLY_NOTI(HttpStatus.BAD_REQUEST, "NOTI400_2", "지원하지 않는 알림 유형입니다."),
    FCM_SEND_FAIL(HttpStatus.BAD_REQUEST, "NOTI400_3", "알림 전송 실패"),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI404_2", "알림을 찾을 수 없습니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "NOTI400_4", "알림 이용 불가"),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "NOTI400_5" ,"존재하지 않는 회원입니다." );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
