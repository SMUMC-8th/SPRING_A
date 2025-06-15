package com.project.teama_be.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import com.project.teama_be.domain.chat.service.command.SendBirdService;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.dto.AuthResDTO;
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
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SendBirdService sendBirdService;
    private final MemberRepository memberRepository;

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
        String loginId = requestBody.loginId();
        String password = requestBody.password();

        log.info("[ Login Filter ] Email ---> {} ", loginId);
        log.info("[ Login Filter ] Password ---> {} ", password);

        // UserNamePasswordToken 생성 (인증용 객체)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginId, password, null);

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
         accessCookie.setSecure(false);
//         accessCookie.setAttribute("SameSite", "Lax");

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(Math.toIntExact(jwtUtil.getRefreshExpMs() / 1000));
        // HTTPS를 사용하는 경우 활성화
         refreshCookie.setSecure(false);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // SendBird 토큰 발급
        ChatResDTO.SendBirdTokenInfo sendBirdToken = null;
        Member member = null;

        try {
            // 회원 정보 조회
            member = memberRepository.findByLoginId(jwtUtil.getLoginId(accessToken))
                    .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

            // SendBird 토큰 발급 (재시도 로직 추가)
            sendBirdToken = sendBirdService.getUserToken(member)
                    .retry(3)  // 최대 3번 재시도
                    .timeout(Duration.ofSeconds(5))  // 5초 타임아웃
                    .block();

//            if (sendBirdToken == null) {
//                // 로그인 실패 처리
//                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
//                response.getWriter().write("채팅 서비스 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
//                return;
//            }

            log.info("[ Login Filter ] SendBird 토큰 발급 성공: {}", member.getId());
        } catch (Exception e) {
            log.error("[ Login Filter ] SendBird 토큰 발급 실패: {}", e.getMessage());
        }

        // 로그인 응답 DTO 생성
        AuthResDTO.Login loginResponse = AuthResDTO.Login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member != null ? member.getId() : null)
                .nickname(member != null ? member.getNickname() : null)
                .profileUrl(member != null ? member.getProfileUrl() : null)
                .sendBirdToken(sendBirdToken)
                .build();

        HttpResponseUtil.setSuccessResponse(response, HttpStatus.OK, loginResponse);
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
