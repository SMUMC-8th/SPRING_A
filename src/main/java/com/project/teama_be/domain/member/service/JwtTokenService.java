package com.project.teama_be.domain.member.service;

import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.util.JwtUtil;
import com.project.teama_be.global.utils.HttpResponseUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenService {

    private final JwtUtil jwtUtil;

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
}
