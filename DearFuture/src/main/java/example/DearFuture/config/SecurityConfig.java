package example.DearFuture.config;

import example.DearFuture.auth.handler.OAuth2LoginSuccessHandler;
import example.DearFuture.auth.jwt.JwtAuthenticationFilter;
import example.DearFuture.auth.service.CustomOAuth2UserService;
import example.DearFuture.ratelimit.GlobalRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final GlobalRateLimitFilter globalRateLimitFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable()) // REST API için CSRF kapalı
            // OAuth2 login akışı için session gerekir (redirect → Google → callback). Diğer istekler JWT ile stateless.
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Açık endpoint'ler (OPTIONS preflight dahil)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/subscription/callback", "/api/subscription/plans").permitAll()
                .requestMatchers("/api/messages/view/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/messages/public").permitAll()
                .requestMatchers("/api/contact/send", "/api/contact/verify-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contracts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contracts/type/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contracts/*").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Google OAuth2 giriş akışı (login sayfası ve callback)
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                // Admin endpoint'leri sadece ADMIN rolüne sahip kullanıcılar
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Public mesajlar: liste herkese açık, yıldız/starred giriş gerekir
                .requestMatchers("/api/messages/public/**").authenticated()
                // Diğer tüm endpointler JWT ile korumalı
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                .successHandler(oauth2LoginSuccessHandler)
            );

        // Both filters before UsernamePasswordAuthenticationFilter (which has registered order). Second add = runs first.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(globalRateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager bean (opsiyonel, JWT kullanıyorsan genelde lazım değil)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
