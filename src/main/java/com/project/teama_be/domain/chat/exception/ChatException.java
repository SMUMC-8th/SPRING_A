package com.project.teama_be.domain.chat.exception;

import com.project.teama_be.global.apiPayload.exception.CustomException;
import lombok.Getter;

public class ChatException extends RuntimeException {
    private final ChatErrorCode error; // 현재 필드 이름

    public ChatException(ChatErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    // 기존 getter가 있다면 유지
    public ChatErrorCode getError() {
        return error;
    }

    // 새 getErrorCode 메서드 추가
    public ChatErrorCode getErrorCode() {
        return error;
    }
}
