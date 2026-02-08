package example.DearFuture.config;

import example.DearFuture.auth.jwt.JwtAuthenticationFilter;
import example.DearFuture.ratelimit.GlobalRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable()) // REST API için CSRF kapalı
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Açık endpoint'ler (OPTIONS preflight dahil)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/subscription/callback", "/api/subscription/plans").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger opsiyonel
                // Diğer tüm endpointler JWT ile korumalı
                .anyRequest().authenticated()
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
