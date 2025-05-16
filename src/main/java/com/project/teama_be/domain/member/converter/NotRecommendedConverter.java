package com.project.teama_be.domain.member.converter;

import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.entity.NotRecommended;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotRecommendedConverter {

    public static NotRecommended toNotRecommended(Member member, MemberReqDTO.blockMember reqDTO) {
        return NotRecommended.builder()
                .member(member)
                .targetMemberId(reqDTO.targetMemberId())
                .build();
    }

    public static MemberResDTO.blockMember toBlockMemberResDTO(NotRecommended notRecommended) {
        return MemberResDTO.blockMember.builder()
                .targetMemberId(notRecommended.getTargetMemberId())
                .build();
    }
}
