package com.project.teama_be.domain.member.controller;

import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.service.command.AuthCommandService;
import com.project.teama_be.domain.member.service.command.JwtTokenService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "인증 관련 API", description = "인증 관련 API입니다.")
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final AuthCommandService authCommandService;
    private final JwtUtil jwtUtil;

    @PostMapping(value = "/api/auth/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입 API by 김지명", description = "사용자 정보와 프로필 이미지를 함께 받아 회원가입을 처리합니다.")
    public CustomResponse<MemberResDTO.SignUp> signUp(
            @RequestPart(value = "SignUp") @Valid MemberReqDTO.SignUp reqDTO,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletResponse response) {
        MemberResDTO.SignUp resDTO = authCommandService.signUp(reqDTO, profileImage);

        // 회원가입 성공 시 자동 로그인 처리
        JwtDTO jwtDTO = authCommandService.createTokensForNewMember(resDTO.memberId());
        setCookies(response, jwtDTO.accessToken(), jwtDTO.refreshToken());

        return CustomResponse.onSuccess(HttpStatus.CREATED, resDTO);
    }

    // Swagger용 컨트롤러
    @PostMapping("/api/auth/login")
    @Operation(summary = "로그인 API by 김지명", description = "아이디와 비밀번호를 검증하고 JWT 토큰을 발급합니다.")
    public CustomResponse<JwtDTO> login(@RequestBody @Valid MemberReqDTO.Login reqDTO) {
        return CustomResponse.onSuccess(null);
    }

    // Swagger용 컨트롤러
    @PostMapping("/api/auth/logout")
    @Operation(summary = "로그아웃 API by 김지명", description = "현재 로그인된 사용자의 토큰을 무효화하고 로그아웃 처리합니다.")
    public CustomResponse<String> logout() {return CustomResponse.onSuccess("로그아웃이 완료되었습니다.");}

    @PostMapping("/api/auth/refresh")
    @Operation(summary = "JWT 재발급 API", description = "쿠키에 담긴 RefreshToken을 검증하고 새 JWT를 발급합니다.")
    public CustomResponse<JwtDTO> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            log.error("[ AuthController ] 쿠키에 refresh_token이 없습니다.");
            return CustomResponse.onFailure("401", "Refresh Token이 존재하지 않습니다.");
        }

        try {
            JwtDTO jwtDTO = jwtTokenService.reissueTokenAndSetCookie(refreshToken, response);
            return CustomResponse.onSuccess(jwtDTO);
        } catch (Exception e) {
            log.error("[ AuthController ] 토큰 재발급 실패: {}", e.getMessage());
            return CustomResponse.onFailure("401", "유효하지 않은 Refresh Token입니다.");
        }
    }

    @GetMapping("/oauth2/callback/kakao")
    @Operation(summary = "카카오 로그인 API", description = "인가코드를 넘겨받으면 사용자의 정보를 리소스 서버에서 가져와 JWT 토큰을 발급")
    public CustomResponse<JwtDTO> loginWithKakao(@RequestParam("code") String code, HttpServletResponse response) {
        log.info("카카오 로그인 콜백 요청 - 인증 코드: {}", code);
        JwtDTO jwtDTO = authCommandService.loginWithKakao(code);
        setCookies(response, jwtDTO.accessToken(), jwtDTO.refreshToken());

        return CustomResponse.onSuccess(jwtDTO);
    }

    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // 액세스 토큰 쿠키
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(Math.toIntExact(jwtUtil.getAccessExpMs() / 1000));
        accessCookie.setSecure(true); // HTTPS에서만 전송

        // 리프레시 토큰 쿠키
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(Math.toIntExact(jwtUtil.getRefreshExpMs() / 1000));
        refreshCookie.setSecure(true); // HTTPS에서만 전송

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}
