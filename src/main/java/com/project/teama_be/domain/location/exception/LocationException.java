package com.project.teama_be.domain.location.exception;

import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import com.project.teama_be.global.apiPayload.exception.CustomException;

public class LocationException extends CustomException {
    public LocationException(BaseErrorCode code) {
        super(code);
    }
}
