package ch.zhaw.shareway.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {

    public static final String ADMIN = "Bearer admin";
    public static final String USER = "Bearer user";
    public static final String DRIVER = "Bearer driver";
    public static final String INVALID = "Bearer invalid";

    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) {
                var bearer = "Bearer " + token;
                if (bearer.equals(ADMIN) || bearer.equals(USER) || bearer.equals(DRIVER)) {
                    return createJwtWithRole(token);
                }
                throw new AuthenticationException("Invalid JWT") {
                };
            }
        };
    }

    private Jwt createJwtWithRole(String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user");
        claims.put("email", "test@test.com");
        claims.put("user_roles", List.of(role));

        return new Jwt(
            "valid-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            claims);
    }
}