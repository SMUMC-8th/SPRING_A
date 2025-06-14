package com.project.teama_be.domain.member.service.command;

import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.util.JwtUtil;
import com.project.teama_be.global.utils.RedisUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public JwtDTO reissueTokenAndSetCookie(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            log.error("[ JwtTokenService ] 쿠키에 refresh_token이 없습니다.");
            throw new IllegalArgumentException("Refresh Token이 존재하지 않습니다.");
        }

        JwtDTO jwt = jwtUtil.reissueToken(refreshToken);

        setCookieSettings(response, "access_token", jwt.accessToken(), jwtUtil.getAccessExpMs());
        setCookieSettings(response, "refresh_token", jwt.refreshToken(), jwtUtil.getRefreshExpMs());

        log.info("[ JwtTokenService ] 토큰 재발급 성공. 쿠키 재설정 완료.");
        return jwt;
    }

    public String getRefreshTokenByLoginId(String loginId) {
        return (String) redisUtil.get(loginId + ":refresh"); // Redis에서 Refresh 토큰 가져오기
    }

    public void deleteRefreshTokenByLoginId(String loginId) {
        log.info("이메일에 대한 토큰을 삭제합니다: {}", loginId);
        redisUtil.delete(loginId + ":refresh"); // Redis에서 Refresh 토큰 삭제
    }

    // 주어진 토큰을 블랙리스트에 추가하고, 주어진 기간 동안 유지
    public void addToBlacklist(String token, long durationMs) {
        String key = BLACKLIST_PREFIX + token;
        redisUtil.save(key, true, durationMs, TimeUnit.MILLISECONDS); // Redis에 블랙리스트 키와 값을 저장하고, 만료 시간 설정(TTL)
    }

    // 주어진 토큰이 블랙리스트에 있는지 확인
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisUtil.hasKey(key));
    }

    private void setCookieSettings(HttpServletResponse response, String name, String value, long expMs) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(expMs / 1000));
        cookie.setSecure(true); // 개발환경용
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }
}
