package com.project.teama_be.domain.member.dto.response;

import java.time.LocalDateTime;

public class MemberResDTO {

    public record SignUp(
            Long memberId,
            String loginId,
            String nickname,
            LocalDateTime createdAt
    ) {
    }

    public record blockMember(
            Long targetMemberId
    ) {
    }

    public record memberInfo(
            Long memberId,
            String loginId,
            String nickname,
            String profileUrl
    ) {
    }

    public record changeNickname(
            String newNickname
    ) {
    }

    public record changeProfileImg(
            String profileImageUrl
    ) {
    }

    public record deleteMember(
            Long memberId,
            LocalDateTime deletedAt
    ) {
    }
}
