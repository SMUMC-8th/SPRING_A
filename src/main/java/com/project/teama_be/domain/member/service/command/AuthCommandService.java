package com.project.teama_be.domain.member.service.command;

import com.project.teama_be.domain.member.converter.MemberConverter;
import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.dto.response.OAuth2DTO;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.enums.LoginType;
import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.global.aws.util.S3Util;
import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.userdetails.CustomUserDetails;
import com.project.teama_be.global.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Util s3Util;
    private final WebClient webClient = WebClient.builder().build();
    private final JwtUtil jwtUtil;
    private static final String PROFILE_IMAGE_FOLDER = "user-image/";
    private static final String DEFAULT_PROFILE_IMAGE_URI = "https://s3.ap-northeast-2.amazonaws.com/api-smp.shop/user-image/TempUser.png";

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenURI;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoURI;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectURI;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    public MemberResDTO.SignUp signUp(MemberReqDTO.SignUp reqDTO, MultipartFile profileImage) {
        if (memberRepository.existsByLoginId(reqDTO.loginId())) {
            throw new MemberException(MemberErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(reqDTO.nickname())) {
            throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 프로필 이미지 S3 업로드 Util 메서드 사용
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 단일 이미지 업로드 메서드 호출
                String imageKey = s3Util.uploadFile(profileImage, PROFILE_IMAGE_FOLDER);
                profileImageUrl = s3Util.getImageUrl(imageKey);
                log.info("[ 회원가입 ] 프로필 이미지 업로드 성공: {}", profileImageUrl);
            } catch (IllegalArgumentException e) {
                log.error("[ 회원가입 ] 프로필 이미지 업로드 실패: {}", e.getMessage());
                throw new MemberException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
            }
        } else {
            log.info("[ 회원가입 ] 프로필 이미지 없음, 기본 이미지 사용");
            profileImageUrl = DEFAULT_PROFILE_IMAGE_URI;
        }

        Member member = MemberConverter.toMember(reqDTO, passwordEncoder, profileImageUrl);
        memberRepository.save(member);

        return MemberConverter.toSignUpResDTO(member);
    }

    public JwtDTO loginWithKakao(String code) {
        try {
            // 1. Access Token 요청
            log.info("Access Token 요청 중...");
            OAuth2DTO.OAuth2TokenDTO tokenDto = getAccessToken(code);

            // 2. 사용자 정보 요청
            log.info("사용자 정보 요청 중...");
            OAuth2DTO.KakaoProfile kakaoProfile = getUserInfo(tokenDto.access_token());

            // 3. 이메일 확인
            String email = kakaoProfile.kakao_account().email();
            log.info("이메일 확인: {}", email);
            if (email == null) {
                throw new MemberException(MemberErrorCode.OAUTH_EMAIL_NOT_FOUND);
            }

            // 4. 데이터베이스에서 사용자 조회 또는 신규 사용자 저장
            log.info("사용자 조회 또는 저장 중...");
            Member member = memberRepository.findByEmail(email)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder()
                                    .email(email)
                                    .loginId(email)
                                    .nickname("임시닉네임_" + System.currentTimeMillis())
                                    .loginType(LoginType.KAKAO)
                                    .isAgree(true)
                                    .profileUrl(DEFAULT_PROFILE_IMAGE_URI)
                                    .build()));

            CustomUserDetails customUserDetails = new CustomUserDetails(member);

            // 5. JWT 토큰 발급
            log.info("JWT 토큰 발급 중...");
            String accessToken = jwtUtil.createJwtAccessToken(customUserDetails);
            String refreshToken = jwtUtil.createJwtRefreshToken(customUserDetails);

            // JwtDTO 반환
            return JwtDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            log.error("카카오 로그인 실패: {}", e.getMessage(), e);
            throw new MemberException(MemberErrorCode.OAUTH_LOGIN_FAIL);
        }
    }

    private OAuth2DTO.OAuth2TokenDTO getAccessToken(String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("redirect_uri", redirectURI);
            formData.add("code", code);
            formData.add("client_secret", clientSecret);

            System.out.println("=== AccessToken 요청 정보 ===");
            System.out.println("토큰 URI: " + tokenURI);
            System.out.println("클라이언트 ID: " + clientId);
            System.out.println("리다이렉트 URI: " + redirectURI);
            System.out.println("인증 코드: " + code);

            return webClient.post()
                    .uri(tokenURI)
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        System.err.println("토큰 요청 오류: " + response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("오류 응답: " + errorBody);
                                    return Mono.error(new RuntimeException("토큰 요청 실패: " + errorBody));
                                });
                    })
                    .bodyToMono(OAuth2DTO.OAuth2TokenDTO.class)
                    .doOnSuccess(token -> System.out.println("토큰 발급 성공: " + token.access_token()))
                    .doOnError(e -> {
                        System.err.println("토큰 요청 오류: " + e.getMessage());
                        e.printStackTrace();
                    })
                    .onErrorMap(e -> new MemberException(MemberErrorCode.OAUTH_TOKEN_FAIL))
                    .block();
        } catch (Exception e) {
            System.err.println("예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw new MemberException(MemberErrorCode.OAUTH_TOKEN_FAIL);
        }
    }

    private OAuth2DTO.KakaoProfile getUserInfo(String accessToken) {
        return webClient.get()
                .uri(userInfoURI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(OAuth2DTO.KakaoProfile.class)
                .onErrorMap(e -> new MemberException(MemberErrorCode.OAUTH_USER_INFO_FAIL)) // 에러 처리
                .block();
    }

    public JwtDTO createTokensForNewMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        String accessToken = jwtUtil.createJwtAccessToken(customUserDetails);
        String refreshToken = jwtUtil.createJwtRefreshToken(customUserDetails);

        return JwtDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
