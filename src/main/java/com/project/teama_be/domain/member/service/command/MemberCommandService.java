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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final NotRecommendedRepository notRecommendedRepository;

    public MemberResDTO.blockMember blockMember(String loginId, MemberReqDTO.blockMember reqDTO) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (memberRepository.existsById(reqDTO.targetMemberId())) {
            throw new MemberException(MemberErrorCode.TARGET_MEMBER_NOT_FOUND);
        }

        NotRecommended notRecommended = NotRecommendedConverter.toNotRecommended(member, reqDTO);
        notRecommendedRepository.save(notRecommended);

        return NotRecommendedConverter.toBlockMemberResDTO(notRecommended);
    }
}
