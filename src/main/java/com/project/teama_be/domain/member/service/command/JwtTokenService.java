package com.project.teama_be.domain.member.service.command;

import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.util.JwtUtil;
import com.project.teama_be.global.utils.HttpResponseUtil;
import com.project.teama_be.global.utils.RedisUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void reissueTokenAndSetCookie(String refreshToken, HttpServletResponse response) throws IOException {

        if (refreshToken == null) {
            log.error("[ JwtTokenService ] 쿠키에 refresh_token이 없습니다.");
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    CustomResponse.onFailure("401", "Refresh Token이 존재하지 않습니다."));
            return;
        }

        try {
            JwtDTO jwt = jwtUtil.reissueToken(refreshToken);

            Cookie accessCookie = new Cookie("access_token", jwt.accessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(Math.toIntExact(jwtUtil.getAccessExpMs() / 1000));
            accessCookie.setSecure(true); // HTTPS 환경

            Cookie refreshCookie = new Cookie("refresh_token", jwt.refreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(Math.toIntExact(jwtUtil.getRefreshExpMs() / 1000));
            refreshCookie.setSecure(true);

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);

            log.info("[ JwtTokenService ] 토큰 재발급 성공. 쿠키 재설정 완료.");
            HttpResponseUtil.setSuccessResponse(response, HttpStatus.OK, null);

        } catch (Exception e) {
            log.error("[ JwtTokenService ] 토큰 재발급 실패: {}", e.getMessage());
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    CustomResponse.onFailure("401", "유효하지 않은 Refresh Token입니다."));
        }
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
}
