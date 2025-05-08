package com.project.teama_be.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberReqDTO {

    public record Login(
            String loginId,
            String password
    ) {
    }

    public record SignUp(
            @NotBlank(message = "아이디는 필수 입력값입니다.")
            @Size(min = 6, max = 12, message = "아이디는 6자 이상 12자 이하로 입력해주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용할 수 있습니다.")
            String loginId,

            @NotBlank(message = "닉네임은 필수 입력값입니다.")
            @Size(max = 8, message = "닉네임은 최대 8자까지 입력할 수 있습니다.")
            String nickname,

            @NotBlank(message = "비밀번호는 필수 입력값입니다.")
            @Size(min = 8, max = 20, message = "비밀번호 길이조건(8~20자)를 만족하지 않습니다.")
            String password
    ) {
    }

    public record blockMember(
            Long targetMemberId
    ) {
    }

    public record changePassword(
            String oldPassword,

            @NotBlank(message = "비밀번호는 필수 입력값입니다.")
            @Size(min = 8, max = 20, message = "비밀번호 길이조건(8~20자)를 만족하지 않습니다.")
            String newPassword
    ) {
    }

    public record changeNickname(
            @NotBlank(message = "닉네임은 필수 입력값입니다.")
            @Size(max = 8, message = "닉네임은 최대 8자까지 입력할 수 있습니다.")
            String newNickname
    ) {
    }
}
