package com.project.teama_be.global.security.handler;

import com.project.teama_be.domain.member.service.command.JwtTokenService;
import com.project.teama_be.global.security.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtTokenService jwtTokenService;
    private final JwtUtil jwtUtil;
    private final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 1. 요청에서 쿠키 추출
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 2. 토큰 처리
        if (accessToken != null) {
            // 액세스 토큰에서 loginId 추출
            String loginId = jwtUtil.getLoginId(accessToken);

            // Redis에서 저장된 리프레시 토큰 가져오기
            if (refreshToken == null) {
                refreshToken = jwtTokenService.getRefreshTokenByLoginId(loginId);
            }

            if (refreshToken != null) {
                // 리프레시 토큰을 블랙리스트에 추가
                long expiryDuration = jwtUtil.getRefreshTokenRemainingTime(refreshToken);
                jwtTokenService.addToBlacklist(refreshToken, expiryDuration);
            }

            // 이메일과 연결된 리프레시 토큰 삭제
            jwtTokenService.deleteRefreshTokenByLoginId(loginId);
        }

        // 3. 클라이언트에서 쿠키 삭제
        deleteTokenCookies(response);
    }

    // 4. 응답에서 토큰 쿠키 삭제
    private void deleteTokenCookies(HttpServletResponse response) {
        // 액세스 토큰 쿠키 삭제
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
        accessTokenCookie.setMaxAge(0); // 즉시 만료
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        response.addCookie(accessTokenCookie);

        // 리프레시 토큰 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        refreshTokenCookie.setMaxAge(0); // 즉시 만료
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);
    }

}
