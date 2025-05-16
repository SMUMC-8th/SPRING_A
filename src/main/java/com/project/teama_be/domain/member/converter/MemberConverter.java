package com.project.teama_be.domain.member.converter;

import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.enums.LoginType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    // SignUpReqDTO -> Member Entity
    public static Member toMember(MemberReqDTO.SignUp reqDTO, PasswordEncoder passwordEncoder, String profileImageUrl) {
        return Member.builder()
                .loginId(reqDTO.loginId())
                .nickname(reqDTO.nickname())
                .password(passwordEncoder.encode(reqDTO.password()))
                .location(null)
                .isAgree(true)
                .loginType(LoginType.LOCAL)
                .profileUrl(profileImageUrl)
                .build();
    }

    public static MemberResDTO.SignUp toSignUpResDTO(Member member) {
        return MemberResDTO.SignUp.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileUrl())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static MemberResDTO.memberInfo toMemberInfoResDTO(Member member) {
        return MemberResDTO.memberInfo.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .profileUrl(member.getProfileUrl())
                .build();
    }

    public static MemberResDTO.changeNickname toChangeNicknameResDTO(Member member) {
        return MemberResDTO.changeNickname.builder()
                .newNickname(member.getNickname())
                .build();
    }

    public static MemberResDTO.changeProfileImg toChangeProfileImgResDTO(Member member) {
        return MemberResDTO.changeProfileImg.builder()
                .memberId(member.getId())
                .profileImageUrl(member.getProfileUrl())
                .build();
    }

    public static MemberResDTO.deleteMember toDeleteMemberResDTO(Member member) {
        return MemberResDTO.deleteMember.builder()
                .memberId(member.getId())
                .build();
    }
}
