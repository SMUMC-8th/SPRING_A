package com.project.teama_be.global.security.filter;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.global.security.exception.SecurityErrorCode;
import com.project.teama_be.global.security.userdetails.CustomUserDetails;
import com.project.teama_be.global.security.util.JwtUtil;
import com.project.teama_be.global.utils.HttpResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    //JWT 토큰을 사용하여 요청을 인증하는 역할
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 🔥 디버그 로그 추가 - 요청 정보
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info("=== JWT Authorization Filter 디버그 시작 ===");
        log.info("Request URI: {}", requestURI);
        log.info("Request Method: {}", method);
        log.info("Request Origin: {}", request.getHeader("Origin"));
        log.info("Request User-Agent: {}", request.getHeader("User-Agent"));

        // 🔥 쿠키 정보 상세 로깅
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info("총 쿠키 개수: {}", cookies.length);
            for (Cookie cookie : cookies) {
                String cookieValue = cookie.getValue();
                String displayValue = cookieValue != null && cookieValue.length() > 20
                        ? cookieValue.substring(0, 20) + "..."
                        : cookieValue;
                log.info("쿠키 - 이름: {}, 값: {}, 도메인: {}, 경로: {}",
                        cookie.getName(), displayValue, cookie.getDomain(), cookie.getPath());
            }
        } else {
            log.warn("🚨 쿠키가 전혀 없습니다! 프론트엔드에서 withCredentials: true 설정 확인 필요");
        }

        log.info("[ JwtAuthorizationFilter ] 인가 필터 작동");

        try {
            // 쿠키에서 access token 추출
            String accessToken = extractTokenFromCookie(request);

            // 🔥 토큰 추출 결과 로깅
            if (accessToken == null) {
                log.warn("🚨 access_token 쿠키를 찾을 수 없습니다!");
                log.warn("사용 가능한 쿠키 목록:");
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        log.warn("  - {}", cookie.getName());
                    }
                } else {
                    log.warn("  - 쿠키 없음");
                }
                log.info("Access Token이 존재하지 않음. 필터를 건너뜁니다.");
                filterChain.doFilter(request, response);
                return;
            } else {
                log.info("✅ access_token 쿠키 발견! 토큰 길이: {}", accessToken.length());
                log.info("토큰 시작 부분: {}", accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
            }

            authenticateAccessToken(accessToken);
            log.info("✅ JWT 인증 성공 - 다음 필터로 진행");
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 토큰 만료 처리
            log.warn("🚨 accessToken이 만료되었습니다: {}", e.getMessage());
            handleException(response, SecurityErrorCode.TOKEN_EXPIRED);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("🚨 토큰 인증 과정에서 오류 발생: {}", e.getMessage(), e);
            handleException(response, SecurityErrorCode.INVALID_TOKEN);
        }

        log.info("=== JWT Authorization Filter 디버그 종료 ===");
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        log.info("[ JwtAuthorizationFilter ] 쿠키에서 access_token 추출 시도");

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.debug("쿠키 확인 중: {}", cookie.getName());
                if ("access_token".equals(cookie.getName())) {
                    log.info("✅ access_token 쿠키 발견");
                    return cookie.getValue();
                }
            }
        }

        log.warn("🚨 access_token 쿠키를 찾을 수 없음");
        return null;
    }

    // 예외 발생 시 HttpResponseUtil 을 사용하여 에러 응답을 처리하는 메서드
    private void handleException(HttpServletResponse response, SecurityErrorCode errorCode) throws IOException {
        log.error("🚨 JWT 인증 실패 - 에러 코드: {}, 메시지: {}",
                errorCode.getCode(), errorCode.getMessage());
        // HttpResponseUtil을 사용하여 에러 응답을 처리
        HttpResponseUtil.setErrorResponse(response, errorCode.getHttpStatus(), errorCode.getErrorResponse());
    }

    //Access 토큰의 유효성을 검사하는 메서드
    private void authenticateAccessToken(String accessToken) {
        log.info("[ JwtAuthorizationFilter ] 토큰으로 인가 과정을 시작합니다.");

        try {
            // AccessToken 유효성 검증
            jwtUtil.validateToken(accessToken);
            log.info("✅ [ JwtAuthorizationFilter ] Access Token 유효성 검증 성공.");

            // 사용자 uid로 User 엔티티 조회
            String loginId = jwtUtil.getLoginId(accessToken);
            log.info("[ JwtAuthorizationFilter ] 토큰에서 추출한 loginId: {}", loginId);

            Member member = memberRepository.findByLoginId(loginId)
                    .orElseThrow(() -> {
                        log.error("🚨 사용자를 찾을 수 없습니다: {}", loginId);
                        return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
                    });

            log.info("✅ [ JwtAuthorizationFilter ] 사용자 조회 성공 - ID: {}, 닉네임: {}",
                    member.getId(), member.getNickname());

            // CustomUserDetail 객체 생성
            CustomUserDetails userDetails = new CustomUserDetails(member);
            log.info("✅ [ JwtAuthorizationFilter ] UserDetails 객체 생성 성공");

            // Spring Security 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            // JWT 기반의 토큰 인증에서는 세션을 사용하지 않기 때문에, SecurityContextHolder 에 현재 인증 객체 저장
            // 다음 요청이 들어올 때마다 새로운 JwtAuthorizationFilter가 작동하여 JWT 토큰을 검증하고,
            // 그 때마다 SecurityContextHolder에 인증 정보를 설정하는 방식으로 동작
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("✅ [ JwtAuthorizationFilter ] 인증 객체 저장 완료 - 사용자: {}", loginId);

        } catch (Exception e) {
            log.error("🚨 [ JwtAuthorizationFilter ] 토큰 인증 중 오류 발생: {}", e.getMessage(), e);
            throw e; // 예외를 다시 던져서 상위에서 처리하도록 함
        }
    }
}