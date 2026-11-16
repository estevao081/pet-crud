package dev.estv.petcrud.gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Apenas VERIFICA tokens (assinatura, issuer e expiração). Quem emite é o auth-service.
 * Mesmo formato do monólito: HS256, issuer "estv", subject = id do usuário, claim "role".
 */
@Component
public class JwtVerifier {

    private final JWTVerifier verifier;

    public JwtVerifier(@Value("${jwt.secret}") String secret) {
        this.verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("estv")
                .build();
    }

    public Optional<TokenClaims> verify(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getSubject();
            String role = jwt.getClaim("role").asString();
            if (userId == null || role == null) {
                return Optional.empty();
            }
            UUID.fromString(userId); // garante que o subject é um UUID (valor seguro para header)
            return Optional.of(new TokenClaims(userId, role));
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
