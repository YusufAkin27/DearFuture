package example.DearFuture.auth.jwt;

import example.DearFuture.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 characters for HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // token üretirken:
    public String generateToken(Long userId, String email, Set<Role> roles) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("roles", roles.stream().map(Enum::name).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 saat
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
