package dev.estv.petcrud.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.estv.petcrud.auth.service.TokenService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only";
    private final TokenService tokenService = new TokenService(SECRET, 2);

    @Test
    void tokenCarriesUserIdAsSubjectAndRoleClaim() {
        String userId = UUID.randomUUID().toString();

        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("estv").build()
                .verify(tokenService.generateToken(userId, "ROLE_ADMIN"));

        assertThat(jwt.getSubject()).isEqualTo(userId);
        assertThat(jwt.getClaim("role").asString()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void tokenExpiresInAboutTwoHoursRegardlessOfTimezone() {
        DecodedJWT jwt = JWT.decode(tokenService.generateToken(UUID.randomUUID().toString(), "ROLE_USER"));

        Duration ttl = Duration.between(Instant.now(), jwt.getExpiresAtAsInstant());

        assertThat(ttl).isBetween(Duration.ofMinutes(119), Duration.ofMinutes(121));
    }
}
