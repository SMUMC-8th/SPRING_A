package com.project.teama_be.global.security.util;

import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.global.security.dto.JwtDTO;
import com.project.teama_be.global.security.userdetails.CustomUserDetails;
import com.project.teama_be.global.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey; //JWT 서명에 사용되는 비밀 키
    private final Long accessExpMs; //액세스 토큰의 만료 시간
    private final Long refreshExpMs; //리프레시 토큰의 만료 시간
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret,
                   @Value("${spring.jwt.token.access-expiration-time}") Long access,
                   @Value("${spring.jwt.token.refresh-expiration-time}") Long refresh,
                   RedisUtil redisUtil,
                   MemberRepository memberRepository) {

        //주어진 시크릿 키 문자열을 바이트 배열로 변환하고, 이를 사용하여 SecretKey 객체 생성
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        accessExpMs = access; // 액세스 토큰 만료 시간 설정
        refreshExpMs = refresh; // 리프레시 토큰 만료 시간 설정
        this.redisUtil = redisUtil;
        this.memberRepository = memberRepository;
    }

    //JWT 토큰을 입력으로 받아 토큰의 subject 로부터 사용자 loginId를 추출하는 메서드
    public String getLoginId(String token) throws SignatureException {
        log.info("[ JwtUtil ] 토큰에서 loginId를 추출합니다.");
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(); //claims의 Subject에서 사용자의 loginId 추출 (Subject): 토큰의 주체 (일반적으로 사용자 ID나 이메일)
    }

    //토큰을 발급하는 메서드
    public String tokenProvider(CustomUserDetails userDetails, Instant expirationTime) {

        log.info("[ JwtUtil ] 토큰을 새로 생성합니다.");

        //현재 시간
        Instant issuedAt = Instant.now();

        //토큰에 부여할 권한
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); //스트림의 모든 요소를 하나의 문자열로 결합

        return Jwts.builder()
                .header() //헤더 부분
                .add("typ", "JWT") //JWT 타입을 추가
                .and()
                .subject(userDetails.getUsername()) //Subject 에 loginId 추가
                .claim("role", authorities) //권한 정보를 클레임에 추가
                .issuedAt(Date.from(issuedAt)) //발행 시간(현재 시간)을 추가
                .expiration(Date.from(expirationTime)) //만료 시간을 추가
                .signWith(secretKey) //서명 정보를 추가
                .compact(); //합치기
    }

    //JWT 액세스 토큰을 생성
    public String createJwtAccessToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(accessExpMs);
        return tokenProvider(customUserDetails, expiration);
    }

    // principalDetails 객체에 대해 새로운 JWT 리프레시 토큰을 생성
    public String createJwtRefreshToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(refreshExpMs);
        String refreshToken = tokenProvider(customUserDetails, expiration);

        //Refresh Token 저장
        redisUtil.save(
                customUserDetails.getUsername() + ":refresh",
                refreshToken,
                refreshExpMs,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    public String resolveAccessToken(HttpServletRequest request) {
        log.info("쿠키에서 토큰을 추출합니다.");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 기존 헤더 방식도 유지 (호환성)
        String tokenFromHeader = request.getHeader("Authorization");
        if (tokenFromHeader != null && tokenFromHeader.startsWith("Bearer ")) {
            return tokenFromHeader.split(" ")[1];
        }
        return null;
    }

    //토큰의 유효성 검사
    public void validateToken(String token) {
        log.info("[ JwtUtil ] 토큰의 유효성을 검증합니다.");
        try {
            // 구문 분석 시스템의 시계가 JWT를 생성한 시스템의 시계 오차 고려
            // 약 3분 허용.
            long seconds = 3 *60;
            boolean isExpired = Jwts
                    .parser()
                    .clockSkewSeconds(seconds)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
            if (isExpired) {
                log.info("만료된 JWT 토큰입니다.");
            }

        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            //원하는 Exception throw
            throw new SecurityException("잘못된 토큰입니다.");
        } catch (ExpiredJwtException e) {
            //원하는 Exception throw
            throw new ExpiredJwtException(null, null, "만료된 JWT 토큰입니다.");
        }
    }

    // AccessToken 유효기간 get
    public long getAccessExpMs() {
        return this.accessExpMs;
    }

    // RefreshToken 유효기간 get
    public long getRefreshExpMs() {
        return this.refreshExpMs;
    }

    //주어진 리프레시 토큰을 기반으로 새로운 액세스 토큰을 발급
    public JwtDTO reissueToken(String refreshToken) throws SignatureException {
        String loginId = getLoginId(refreshToken);

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        // CustomUserDetails 생성 시 User 객체 사용
        CustomUserDetails userDetails = new CustomUserDetails(member);
        log.info("[ JwtUtil ] 새로운 토큰을 재발급 합니다.");

        return new JwtDTO(
                createJwtAccessToken(userDetails),
                createJwtRefreshToken(userDetails)
        );
    }

    public long getRefreshTokenRemainingTime(String refreshToken) {
        log.info("[ JwtUtil ] 리프레시 토큰의 남은 만료 시간을 계산합니다.");
        try {
            // 토큰에서 만료 시간(expiration) 클레임 추출
            Date expiration = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload()
                    .getExpiration();

            // 현재 시간과의 차이 계산 (밀리초)
            long now = System.currentTimeMillis();
            long expirationTime = expiration.getTime();
            long remainingTimeMs = expirationTime - now;

            // 초 단위로 변환하여 반환 (음수인 경우 0 반환)
            return Math.max(0, remainingTimeMs / 1000);

        } catch (ExpiredJwtException e) {
            log.info("[ JwtUtil ] 이미 만료된 리프레시 토큰입니다.");
            return 0;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error("[ JwtUtil ] 리프레시 토큰 검증 중 오류 발생: {}", e.getMessage());
            return 0;
        }
    }
}
