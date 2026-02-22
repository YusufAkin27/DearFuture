package example.DearFuture.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis sadece rate limit açıkken yüklenir. app.rate-limit.enabled=true ise Redis bağlantısı kurulur.
 * RedisAutoConfiguration kullanılmıyor (paket Spring Boot sürümünde olmayabilir); bean'ler manuel tanımlanıyor.
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true")
public class RedisRateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Bean adı "redisTemplate" olmalı; bazı bileşenler (örn. redisReferenceResolver) bu ada göre arar.
     * StringRedisTemplate, RedisTemplate&lt;String, String&gt; alt sınıfıdır; rate limit ve diğer kullanımlar için uygundur.
     */
    @Bean(name = "redisTemplate")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
