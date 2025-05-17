package com.project.teama_be.domain.notification.exception;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import com.project.teama_be.global.apiPayload.exception.CustomException;

public class NotificationException extends CustomException {

    public NotificationException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
