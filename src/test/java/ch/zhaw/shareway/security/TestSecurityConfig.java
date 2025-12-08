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
    public static final String INVALID = "Bearer invalid";

    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) {
                var bearer = "Bearer " + token;
                if (bearer.equals(ADMIN)) {
                    return createJwt("admin", "admin@test.com", List.of("admin"));
                }
                if (bearer.equals(USER)) {
                    return createJwt("user", "user@test.com", List.of("user"));
                }
                throw new AuthenticationException("Invalid JWT") {};
            }
        };
    }

    private Jwt createJwt(String subject, String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("email", email);
        claims.put("user_roles", roles);

        return new Jwt(
            "valid-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            claims);
    }
}