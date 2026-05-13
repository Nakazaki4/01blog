package com.zone01._blog.auth;

import com.zone01._blog.user.Role;
import com.zone01._blog.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-32";
    private static final long EXPIRATION_MS = 60_000L;

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
        user = User.builder()
                .id(42L)
                .username("alice")
                .email("alice@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void issue_thenParse_roundTripsClaims() {
        String token = jwtService.issue(user);

        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.get("email", String.class)).isEqualTo("alice@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void parse_rejectsTamperedToken() {
        String token = jwtService.issue(user);
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThatThrownBy(() -> jwtService.parse(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parse_rejectsTokenSignedWithDifferentSecret() {
        JwtService other = new JwtService("other-secret-other-secret-other-secret-32", EXPIRATION_MS);
        String foreignToken = other.issue(user);

        assertThatThrownBy(() -> jwtService.parse(foreignToken))
                .isInstanceOf(JwtException.class);
    }
}
