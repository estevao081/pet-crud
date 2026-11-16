package dev.estv.petcrud.gateway.filter;

import dev.estv.petcrud.gateway.security.JwtVerifier;
import dev.estv.petcrud.gateway.security.PublicRoutes;
import dev.estv.petcrud.gateway.security.TokenClaims;
import dev.estv.petcrud.gateway.util.JsonErrors;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Optional;

/**
 * Único ponto onde o JWT é validado. Depois de validar, o gateway repassa a identidade
 * aos serviços em dois headers internos (X-User-Id e X-User-Role).
 * <p>
 * SEGURANÇA: esses headers são SEMPRE removidos da requisição que chega de fora, senão um
 * cliente poderia se passar por qualquer usuário enviando "X-User-Role: ROLE_ADMIN".
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (looksLikePathTraversal(request.getURI().getRawPath())) {
            return JsonErrors.write(exchange, HttpStatus.BAD_REQUEST, "Bad request");
        }

        Optional<TokenClaims> claims = bearerToken(request).flatMap(jwtVerifier::verify);
        boolean isPublic = PublicRoutes.isPublic(request.getMethod(), request.getPath().value());

        if (!isPublic && claims.isEmpty()) {
            return JsonErrors.write(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        ServerHttpRequest mutated = request.mutate().headers(headers -> {
            headers.remove(USER_ID_HEADER);
            headers.remove(USER_ROLE_HEADER);
            claims.ifPresent(c -> {
                headers.set(USER_ID_HEADER, c.userId());
                headers.set(USER_ROLE_HEADER, c.role());
            });
        }).build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Optional<String> bearerToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(header.substring(7));
    }

    /** Impede truques como /images/../internal/... para alcançar rotas que não são públicas. */
    private boolean looksLikePathTraversal(String rawPath) {
        String p = rawPath.toLowerCase(Locale.ROOT);
        return p.contains("..") || p.contains("%2e") || p.contains("%2f") || p.contains("%5c") || p.contains("//");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
