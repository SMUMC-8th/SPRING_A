package com.project.teama_be.domain.member.exceptioin;

import com.project.teama_be.global.apiPayload.exception.CustomException;
import lombok.Getter;

@Getter
public class MemberException extends CustomException {

    public MemberException(MemberErrorCode errorCode){
        super(errorCode);
    }
}
