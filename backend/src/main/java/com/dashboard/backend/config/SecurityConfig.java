package com.dashboard.backend.config;

import com.dashboard.backend.util.CollectRateLimiter;
import com.dashboard.backend.util.JwtTokenProvider;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.DispatcherType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CollectRateLimiter collectRateLimiter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // SSE 응답은 ASYNC dispatch로 처리되므로 별도 허용 필요 (없으면 SSE 스트림이 403)
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // 외부 사이트에 심긴 tracker.js는 JWT 토큰 없이 호출하므로 인증 제외
                        .requestMatchers(req -> "POST".equals(req.getMethod()) && "/api/collect".equals(req.getRequestURI())).permitAll()
                        // 게스트는 쓰기 작업 불가
                        .requestMatchers(req -> "POST".equals(req.getMethod()) && req.getRequestURI().startsWith("/api/projects")).hasRole("USER")
                        .requestMatchers(req -> "DELETE".equals(req.getMethod()) && req.getRequestURI().startsWith("/api/projects")).hasRole("USER")
                        .requestMatchers(req -> "PUT".equals(req.getMethod()) && req.getRequestURI().startsWith("/api/user")).hasRole("USER")
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new AuthRateLimitFilter(objectMapper),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new CollectRateLimitFilter(collectRateLimiter, objectMapper),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // /api/collect: 외부 사이트 어디서든 트래킹 데이터를 보낼 수 있어야 하므로 전체 허용
        // allowCredentials=false 이므로 * 사용 가능
        CorsConfiguration collectConfig = new CorsConfiguration();
        collectConfig.setAllowedOriginPatterns(List.of("*"));
        collectConfig.setAllowedMethods(List.of("POST", "OPTIONS"));
        collectConfig.setAllowedHeaders(List.of("Content-Type"));
        collectConfig.setAllowCredentials(false);
        source.registerCorsConfiguration("/api/collect", collectConfig);

        // 나머지 API: 대시보드 전용, 환경변수로 허용 출처 지정
        // allowCredentials=true 이면 * 사용 불가 → 명시적 origin 필수
        CorsConfiguration config = new CorsConfiguration();
        String allowedOrigins = System.getenv().getOrDefault("ALLOWED_ORIGINS", "http://localhost:5173");
        config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
