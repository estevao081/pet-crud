package dev.estv.petcrud.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Emite JWTs no mesmo formato do monólito (HS256, issuer "estv", subject = id, claim "role"),
 * então tokens e frontend continuam compatíveis. A validação mora no gateway.
 */
@Service
public class TokenService {

    private final String secret;
    private final Duration expiration;

    public TokenService(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration-hours:2}") long expirationHours) {
        this.secret = secret;
        this.expiration = Duration.ofHours(expirationHours);
    }

    public String generateToken(String userId, String role) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer("estv")
                .withSubject(userId)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(expiration)) // Instant é absoluto: independe do fuso do servidor
                .sign(Algorithm.HMAC256(secret));
    }
}
