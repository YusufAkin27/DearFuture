package example.DearFuture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000",
            "https://sandbox-cpp.iyzipay.com",
            "https://sandbox-merchant.iyzipay.com",
            "https://sandbox-merchantgw.iyzipay.com",
            "https://cpp.iyzipay.com",
            "https://merchant.iyzipay.com",
            "https://www.iyzipay.com"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // iyzico callback: herhangi bir origin (iyzico sayfasından yönlendirme). Credentials gerekmez.
        CorsConfiguration callbackConfig = new CorsConfiguration();
        callbackConfig.setAllowedOriginPatterns(List.of("*"));
        callbackConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        callbackConfig.setAllowedHeaders(List.of("*"));
        callbackConfig.setAllowCredentials(false);
        callbackConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/api/subscription/callback", callbackConfig);

        // Diğer API'ler: belirli origin'ler, credentials ile
        CorsConfiguration defaultConfig = new CorsConfiguration();
        defaultConfig.setAllowedOriginPatterns(ALLOWED_ORIGINS);
        defaultConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        defaultConfig.setAllowedHeaders(List.of("*"));
        defaultConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", defaultConfig);

        return source;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(ALLOWED_ORIGINS.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
