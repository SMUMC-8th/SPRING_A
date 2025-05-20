package com.project.teama_be.global.security.config;

import com.project.teama_be.domain.chat.service.command.SendBirdService;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.member.service.command.JwtTokenService;
import com.project.teama_be.global.config.CorsConfig;
import com.project.teama_be.global.security.exception.JwtAccessDeniedHandler;
import com.project.teama_be.global.security.exception.JwtAuthenticationEntryPoint;
import com.project.teama_be.global.security.filter.CustomLoginFilter;
import com.project.teama_be.global.security.filter.JwtAuthorizationFilter;
import com.project.teama_be.global.security.handler.CustomLogoutHandler;
import com.project.teama_be.global.security.util.JwtUtil;
import com.project.teama_be.global.utils.HttpResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtTokenService jwtTokenService;
    private final SendBirdService sendBirdService;

    //인증이 필요하지 않은 url
    private final String[] allowedUrls = {
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/members/check-id",
            "/api/members/check-nickname",
            "/ws-stomp/**",  // WebSocket 관련 모든 경로 추가
            "/ws-stomp/info", // SockJS의 정보 엔드포인트 추가
            "/oauth2/callback/kakao",
            "/oauth2/authorization/kakao"
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS 정책 설정
        http
                .cors(cors -> cors
                        .configurationSource(CorsConfig.apiConfigurationSource()));

        // CSRF 토큰 설정 - 쿠키 사용 시 필요
//        http
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/signup")
//                );
        http.csrf(AbstractHttpConfigurer::disable); // 테스트 용도로만!

        // form 로그인 방식 비활성화 -> REST API 로그인을 사용할 것이기 때문에
        http
                .formLogin(AbstractHttpConfigurer::disable);

        // http basic 인증 방식 비활성화
        http
                .httpBasic(AbstractHttpConfigurer::disable);

        // 세션을 사용하지 않음. (세션 생성 정책을 Stateless 설정.)
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        // OAuth2 로그인 설정 추가
        http
                .oauth2Login(Customizer.withDefaults()
                );

        // 경로별 인가
        http
                .authorizeHttpRequests(auth -> auth
                        //위에서 정의했던 allowedUrls 들은 인증이 필요하지 않음 -> permitAll
                        .requestMatchers(allowedUrls).permitAll()
                        .anyRequest().authenticated() // 그 외의 url 들은 인증이 필요함
                );

        // CustomLoginFilter 인스턴스를 생성하고 필요한 의존성을 주입
        CustomLoginFilter customLoginFilter = new CustomLoginFilter(
                authenticationManager(authenticationConfiguration), jwtUtil, sendBirdService, memberRepository);
        // Login Filter URL 지정
        customLoginFilter.setFilterProcessesUrl("/api/auth/login");
        // 필터 체인에 CustomLoginFilter를 UsernamePasswordAuthenticationFilter 자리에서 동작하도록 추가
        http
                .addFilterAt(customLoginFilter, UsernamePasswordAuthenticationFilter.class);
        // JwtFilter를 CustomLoginFilter 앞에서 동작하도록 필터 체인에 추가
        http
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil, memberRepository), CustomLoginFilter.class);

        // Logout Handler 추가
        http
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(new CustomLogoutHandler(jwtTokenService, jwtUtil))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            try {
                                HttpResponseUtil.setSuccessResponse(
                                        response,
                                        HttpStatus.OK,
                                        "로그아웃이 완료되었습니다."
                                );
                            } catch (IOException e) {
                                // 예외 처리
                                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                            }
                        })
                );


        return http.build();
    }
}
