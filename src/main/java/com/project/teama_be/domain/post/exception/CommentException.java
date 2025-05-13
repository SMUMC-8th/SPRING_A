package com.project.teama_be.domain.post.exception;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import com.project.teama_be.global.apiPayload.exception.CustomException;

public class CommentException extends CustomException {
    public CommentException(BaseErrorCode code) {
        super(code);
    }
}
