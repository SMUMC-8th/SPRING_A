package com.project.teama_be.domain.chat.exception;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT404_0", "채팅방을 찾을 수 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT404_1", "채팅방 참여자를 찾을 수 없습니다."),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT404_2", "위치 정보를 찾을 수 없습니다."),
    SENDBIRD_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT500_0", "SendBird API 호출 중 오류가 발생했습니다."),
    SENDBIRD_TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT500_1", "SendBird 토큰 발급 중 오류가 발생했습니다."),
    SENDBIRD_CHANNEL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT500_2", "SendBird 채널 생성 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
