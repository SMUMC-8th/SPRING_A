package com.project.teama_be.global.security.userdetails;

import com.project.teama_be.domain.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails extends AuthUser implements UserDetails {

    //인증용 객체 생성자
    public CustomUserDetails(Member member) {
        super(member.getId(), member.getLoginId(), member.getPassword());
    }

    // 권한을 반환하는 메서드, 현재는 빈 컬렉션을 반환 (권한이 필요하다면 여기에 추가 가능)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 빈 collection 반환
    }

    // 비밀번호 반환
    @Override
    public String getPassword() {
        return super.getPassword(); // AuthUser 클래스의 getPassword 메서드 호출
    }

    // 이메일을 사용자 이름으로 반환
    @Override
    public String getUsername() {
        return super.getLoginId(); // AuthUser 클래스의 getLoginId 메서드 호출
    }

    // 계정이 만료되지 않았음을 반환
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠기지 않았음을 반환
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명이 만료되지 않았음을 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화되어 있음을 반환
    @Override
    public boolean isEnabled() {
        return true;
    }
}
