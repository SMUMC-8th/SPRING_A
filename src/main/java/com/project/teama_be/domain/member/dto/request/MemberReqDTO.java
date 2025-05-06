package com.project.teama_be.domain.member.dto.request;

public class MemberReqDTO {

    public record Login(
            String loginId,
            String password
    ) {
    }

    public record SignUp(
            String loginId,
            String nickname,
            String password
    ) {
    }

    public record blockMember(
            Long targetMemberId
    ) {
    }

    public record changePassword(
            String oldPassword,
            String newPassword
    ) {
    }

    public record changeNickname(
            String oldNickname,
            String newNickname
    ) {
    }
}
