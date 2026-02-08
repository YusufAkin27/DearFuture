package example.DearFuture.cloudinary.config;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloudinary konfigürasyonu – fotoğraf, ses, video depolama.
 * Credential'lar application.properties veya env (app.cloudinary.*) ile verilir.
 */
@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);

        Cloudinary cloudinary = new Cloudinary(config);
        log.info("Cloudinary yapılandırıldı: cloud_name={}", cloudName);
        return cloudinary;
    }
}

