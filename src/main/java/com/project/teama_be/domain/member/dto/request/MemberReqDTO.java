package com.project.teama_be.domain.member.dto.request;

public class MemberReqDTO {

    public record Login(
            String uid,
            String password
    ) {
    }
}
