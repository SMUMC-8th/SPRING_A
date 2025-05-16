package com.project.teama_be.domain.member.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

public class MemberResDTO {

    @Builder
    public record SignUp(
            Long memberId,
            String loginId,
            String nickname,
            String profileImageUrl,
            LocalDateTime createdAt
    ) {
    }

    @Builder
    public record blockMember(
            Long targetMemberId
    ) {
    }

    @Builder
    public record memberInfo(
            Long memberId,
            String loginId,
            String nickname,
            String profileUrl
    ) {
    }

    @Builder
    public record changeNickname(
            String newNickname
    ) {
    }

    @Builder
    public record changeProfileImg(
            Long memberId,
            String profileImageUrl
    ) {
    }

    @Builder
    public record deleteMember(
            Long memberId
    ) {
    }
}
