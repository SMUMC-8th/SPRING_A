package com.project.teama_be.domain.post.exception;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import com.project.teama_be.global.apiPayload.exception.CustomException;

public class PostException extends CustomException {
    public PostException(BaseErrorCode code) {
        super(code);
    }
}
