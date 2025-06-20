package com.project.teama_be.domain.member.service.query;

import com.project.teama_be.domain.member.converter.MemberConverter;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberResDTO.memberInfo getMemberInfo(AuthUser authUser) {
        Member member = memberRepository.findByLoginId(authUser.getLoginId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberConverter.toMemberInfoResDTO(member);
    }

    public void checkDuplicateId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new MemberException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }
    }

    public void checkDuplicateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
