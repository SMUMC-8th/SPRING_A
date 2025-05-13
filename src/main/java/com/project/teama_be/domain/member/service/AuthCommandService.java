package com.project.teama_be.domain.member.service;

import com.project.teama_be.domain.member.converter.MemberConverter;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResDTO.SignUp signUp(MemberReqDTO.SignUp reqDTO, MultipartFile profileImage) {
        if (memberRepository.existsByLoginId(reqDTO.loginId())) {
            throw new MemberException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(reqDTO.nickname())) {
            throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 프로필 이미지 S3 업로드 Util 메서드 사용
        String profileImageUrl = null;

        Member member = MemberConverter.toMember(reqDTO, passwordEncoder, profileImageUrl);
        memberRepository.save(member);

        return MemberConverter.toSignUpResDTO(member);
    }
}
