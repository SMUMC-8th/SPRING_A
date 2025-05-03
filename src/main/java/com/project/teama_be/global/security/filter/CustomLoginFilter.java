package com.project.teama_be.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.exception.SecurityErrorCode;
import com.project.teama_be.global.security.userdetails.CustomUserDetails;
import com.project.teama_be.global.security.util.JwtUtil;
import com.project.teama_be.global.utils.HttpResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(@NonNull HttpServletRequest request,
                                                @NonNull HttpServletResponse response) throws AuthenticationException {

        log.info("[ Login Filter ]  로그인 시도: Custom Login Filter 작동 ");
        ObjectMapper objectMapper = new ObjectMapper();
        MemberReqDTO.Login requestBody;

        try {
            // Request Body를 읽어 DTO로 변환
            requestBody = objectMapper.readValue(request.getInputStream(), MemberReqDTO.Login.class);
        } catch (IOException e) {
            log.error("[ Login Filter ] Request Body 파싱 중 IOException 발생: {}", e.getMessage());
            throw new AuthenticationServiceException("Request Body 파싱 중 오류가 발생하였습니다.");
        }

        // Request Body에서 uid과 비밀번호 추출
        String uid = requestBody.uid();
        String password = requestBody.password();

        log.info("[ Login Filter ] Email ---> {} ", uid);
        log.info("[ Login Filter ] Password ---> {} ", password);

        // UserNamePasswordToken 생성 (인증용 객체)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(uid, password, null);

        log.info("[ Login Filter ] 인증용 객체 UsernamePasswordAuthenticationToken 이 생성되었습니다. ");
        log.info("[ Login Filter ] 인증을 시도합니다.");

        // 인증 시도
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(@NonNull HttpServletRequest request,
                                            @NonNull HttpServletResponse response,
                                            @NonNull FilterChain chain,
                                            @NonNull Authentication authentication) throws IOException {

        log.info("[ Login Filter ] 로그인에 성공하였습니다.");

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        // JWT 토큰 생성
        String accessToken = jwtUtil.createJwtAccessToken(customUserDetails);
        String refreshToken = jwtUtil.createJwtRefreshToken(customUserDetails);

        // 쿠키에 JWT 토큰 설정
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);  // JavaScript에서 접근 불가
        accessCookie.setPath("/");      // 모든 경로에서 쿠키 접근 가능
        accessCookie.setMaxAge(Math.toIntExact(jwtUtil.getAccessExpMs() / 1000));
        // HTTPS를 사용하는 경우 활성화
        // accessCookie.setSecure(true);
        // accessCookie.setAttribute("SameSite", "Strict");

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(Math.toIntExact(jwtUtil.getRefreshExpMs() / 1000));
        // refreshCookie.setSecure(true);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 응답 바디에는 토큰 없이 성공 메시지만 전송
        JwtDTO jwtInfo = JwtDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        HttpResponseUtil.setSuccessResponse(response, HttpStatus.OK, jwtInfo);
    }

    @Override
    protected void unsuccessfulAuthentication(@NonNull HttpServletRequest request,
                                              @NonNull HttpServletResponse response,
                                              @NonNull AuthenticationException failed) throws IOException {

        log.info("[ Login Filter ] 로그인에 실패하였습니다.");

        SecurityErrorCode errorCode = getErrorCode(failed);

        log.error("[ Login Filter ] 인증 실패: {}", errorCode.getMessage());

        // 실패 응답 처리
        HttpResponseUtil.setErrorResponse(response, errorCode.getHttpStatus(),
                CustomResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }

    private SecurityErrorCode getErrorCode(AuthenticationException failed) {
        if (failed instanceof BadCredentialsException) {
            return SecurityErrorCode.BAD_CREDENTIALS;
        } else if (failed instanceof LockedException || failed instanceof DisabledException) {
            return SecurityErrorCode.FORBIDDEN;
        } else if (failed instanceof UsernameNotFoundException) {
            return SecurityErrorCode.USER_NOT_FOUND;
        } else if (failed instanceof AuthenticationServiceException) {
            return SecurityErrorCode.INTERNAL_SECURITY_ERROR;
        } else {
            return SecurityErrorCode.UNAUTHORIZED;
        }
    }
}
