package com.project.teama_be.global.aws.exception;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import com.project.teama_be.global.apiPayload.exception.CustomException;

public class S3Exception extends CustomException {
    public S3Exception(BaseErrorCode code) {
        super(code);
    }
}
