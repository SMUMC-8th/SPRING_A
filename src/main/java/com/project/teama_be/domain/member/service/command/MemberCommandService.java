package com.project.teama_be.domain.member.service.command;

import com.project.teama_be.domain.member.converter.NotRecommendedConverter;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.entity.NotRecommended;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.member.repository.NotRecommendedRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final NotRecommendedRepository notRecommendedRepository;
    private final PasswordEncoder passwordEncoder;

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

    private Member findMemberByAuthUser(AuthUser authUser) {
         return memberRepository.findByLoginId(authUser.getLoginId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
