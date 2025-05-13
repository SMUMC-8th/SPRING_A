package com.project.teama_be.domain.member.service;

import com.project.teama_be.domain.member.converter.MemberConverter;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.global.aws.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Util s3Util;
    private static final String PROFILE_IMAGE_FOLDER = "profiles/";

    public MemberResDTO.SignUp signUp(MemberReqDTO.SignUp reqDTO, MultipartFile profileImage) {
        if (memberRepository.existsByLoginId(reqDTO.loginId())) {
            throw new MemberException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(reqDTO.nickname())) {
            throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 프로필 이미지 S3 업로드 Util 메서드 사용
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 단일 이미지 업로드 메서드 호출
                String imageKey = s3Util.uploadFile(profileImage, PROFILE_IMAGE_FOLDER);
                profileImageUrl = s3Util.getImageUrl(imageKey);
                log.info("[ 회원가입 ] 프로필 이미지 업로드 성공: {}", profileImageUrl);
            } catch (IllegalArgumentException e) {
                log.error("[ 회원가입 ] 프로필 이미지 업로드 실패: {}", e.getMessage());
                throw new MemberException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
            }
        } else {
            log.info("[ 회원가입 ] 프로필 이미지 없음, 기본 이미지 사용");
            // 기본 프로필 이미지 URL 설정
            // profileImageUrl = "https://your-bucket.s3.region.amazonaws.com/profiles/default.png";
        }

        Member member = MemberConverter.toMember(reqDTO, passwordEncoder, profileImageUrl);
        memberRepository.save(member);

        return MemberConverter.toSignUpResDTO(member);
    }
}
