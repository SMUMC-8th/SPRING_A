package com.project.teama_be.domain.chat.exception;

import com.project.teama_be.global.apiPayload.exception.CustomException;
import lombok.Getter;

@Getter
public class ChatException extends CustomException {

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
    }
}
