package com.project.teama_be.domain.member.service.command;

import com.project.teama_be.domain.member.converter.MemberConverter;
import com.project.teama_be.domain.member.converter.NotRecommendedConverter;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.entity.NotRecommended;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.member.repository.NotRecommendedRepository;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostImage;
import com.project.teama_be.global.aws.util.S3Util;
import com.project.teama_be.global.security.userdetails.AuthUser;
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
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final NotRecommendedRepository notRecommendedRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Util s3Util;
    private static final String PROFILE_IMAGE_FOLDER = "user-image/";
    private static final String DEFAULT_PROFILE_IMAGE_URI = "https://s3.ap-northeast-2.amazonaws.com/api-smp.shop/user-image/TempUser.png";

    public MemberResDTO.blockMember blockMember(AuthUser authUser, MemberReqDTO.blockMember reqDTO) {
        Member member = findMemberByAuthUser(authUser);

        if (!memberRepository.existsById(reqDTO.targetMemberId())) {
            throw new MemberException(MemberErrorCode.TARGET_MEMBER_NOT_FOUND);
        }

        NotRecommended notRecommended = NotRecommendedConverter.toNotRecommended(member, reqDTO);
        notRecommendedRepository.save(notRecommended);

        return NotRecommendedConverter.toBlockMemberResDTO(notRecommended);
    }

    public void changePassword(AuthUser authUser, MemberReqDTO.changePassword reqDTO) {
        Member member = findMemberByAuthUser(authUser);

        if (!passwordEncoder.matches(reqDTO.oldPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.CURRENT_PASSWORD_NOT_MATCH);
        }

        if (reqDTO.oldPassword().equals(reqDTO.newPassword())) {
            throw new MemberException(MemberErrorCode.NEW_PASSWORD_SAME_AS_CURRENT);
        }

        String encodedPassword = passwordEncoder.encode(reqDTO.newPassword());
        member.updatePassword(encodedPassword);
    }

    public MemberResDTO.changeNickname changeNickname(AuthUser authUser, MemberReqDTO.changeNickname reqDTO) {
        Member member = findMemberByAuthUser(authUser);

        if (reqDTO.newNickname().equals(member.getNickname())) {
            throw new MemberException(MemberErrorCode.NEW_NICKNAME_SAME_AS_CURRENT);
        }

        member.updateNickname(reqDTO.newNickname());
        return MemberConverter.toChangeNicknameResDTO(member);
    }

    public MemberResDTO.changeProfileImg changeProfileImg(AuthUser authUser, MultipartFile profileImage) {
        Member member = findMemberByAuthUser(authUser);

        if (profileImage == null || profileImage.isEmpty()) {
            log.error("[ 프로필 변경 ] 프로필 이미지가 없습니다.");
            throw new MemberException(MemberErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        try {
            // 기존 프로필 이미지 URL 저장
            String oldProfileUrl = member.getProfileUrl();

            // 새 프로필 이미지 업로드
            String imageKey = s3Util.uploadFile(profileImage, PROFILE_IMAGE_FOLDER);
            String profileImageUrl = s3Util.getImageUrl(imageKey);
            log.info("[ 프로필 변경 ] 프로필 이미지 업로드 성공: {}", profileImageUrl);

            // 회원 프로필 이미지 URL 업데이트
            member.updateProfileUrl(profileImageUrl);

            // 기존 이미지가 기본 이미지가 아닌 경우 삭제
            if (oldProfileUrl != null && !oldProfileUrl.equals(DEFAULT_PROFILE_IMAGE_URI)) {
                try {
                    s3Util.deleteFile(oldProfileUrl);
                    log.info("[ 프로필 변경 ] 기존 프로필 이미지 삭제 성공: {}", oldProfileUrl);
                } catch (IllegalArgumentException e) {
                    // 기존 이미지 삭제 실패해도 새 이미지 업로드는 성공했으므로 오류 로깅만 하고 계속 진행
                    log.warn("[ 프로필 변경 ] 기존 프로필 이미지 삭제 실패: {}", e.getMessage());
                }
            }

            return MemberConverter.toChangeProfileImgResDTO(member);

        } catch (IllegalArgumentException e) {
            log.error("[ 프로필 변경 ] 프로필 이미지 업로드 실패: {}", e.getMessage());
            throw new MemberException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }
    }

    public MemberResDTO.deleteMember deleteMember(AuthUser authUser) {
        Member member = findMemberByAuthUser(authUser);
        MemberResDTO.deleteMember resDTO = MemberConverter.toDeleteMemberResDTO(member);

        // S3에 저장된 프로필 이미지 처리
        String profileUrl = member.getProfileUrl();
        if (profileUrl != null && !profileUrl.equals(DEFAULT_PROFILE_IMAGE_URI)) {
            try {
                s3Util.deleteFile(profileUrl);
            } catch (Exception e) {
                log.warn("프로필 이미지 삭제 실패: {}", e.getMessage());
            }
        }

        // S3에 저장된 게시물 이미지 삭제 처리
        for (Post post : member.getPosts()) {
            for (PostImage image : post.getPostImages()) {
                try {
                    s3Util.deleteFile(image.getImageUrl());
                } catch (Exception e) {
                    log.warn("게시물 이미지 삭제 실패: {}", e.getMessage());
                }
            }
        }

        // 회원 삭제 (cascade로 연관 엔티티들도 모두 삭제됨)
        memberRepository.delete(member);

        return resDTO;
    }

    private Member findMemberByAuthUser(AuthUser authUser) {
         return memberRepository.findByLoginId(authUser.getLoginId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
