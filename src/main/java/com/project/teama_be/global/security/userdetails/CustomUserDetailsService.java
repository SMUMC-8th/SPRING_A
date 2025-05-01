package com.project.teama_be.global.security.userdetails;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 이메일을 통해 사용자 정보를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        // users 테이블에서 Uid로 사용자 정보 조회
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 존재하지 않습니다."));

        // CustomUserDetails로 변환하여 반환
        return new CustomUserDetails(member);
    }

}
