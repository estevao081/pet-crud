package dev.estv.petcrud.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.estv.petcrud.gateway.filter.JwtAuthenticationFilter;
import dev.estv.petcrud.gateway.security.JwtVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only";

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtVerifier(SECRET));
    private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
    private final GatewayFilterChain chain = ex -> {
        forwarded.set(ex);
        return Mono.empty();
    };

    private String token(String userId, String role, String secret) {
        return JWT.create()
                .withIssuer("estv")
                .withSubject(userId)
                .withClaim("role", role)
                .withExpiresAt(Instant.now().plusSeconds(60))
                .sign(Algorithm.HMAC256(secret));
    }

    @Test
    void protectedRouteWithoutTokenReturns401() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.delete("/pets/1").build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(forwarded.get()).isNull();
    }

    @Test
    void tokenSignedWithAnotherSecretReturns401() {
        String forged = token(UUID.randomUUID().toString(), "ROLE_ADMIN", "another-secret");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/admin").header("Authorization", "Bearer " + forged).build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(forwarded.get()).isNull();
    }

    @Test
    void validTokenForwardsIdentityHeaders() {
        String userId = UUID.randomUUID().toString();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/admin")
                        .header("Authorization", "Bearer " + token(userId, "ROLE_ADMIN", SECRET)).build());

        filter.filter(exchange, chain).block();

        var headers = forwarded.get().getRequest().getHeaders();
        assertThat(headers.getFirst("X-User-Id")).isEqualTo(userId);
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void spoofedIdentityHeadersAreStrippedFromRequestsWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/pets")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ROLE_ADMIN").build());

        filter.filter(exchange, chain).block();

        var headers = forwarded.get().getRequest().getHeaders();
        assertThat(headers.containsKey("X-User-Id")).isFalse();
        assertThat(headers.containsKey("X-User-Role")).isFalse();
    }

    @Test
    void spoofedHeadersAreOverwrittenByTheTokenIdentity() {
        String userId = UUID.randomUUID().toString();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/admin")
                        .header("Authorization", "Bearer " + token(userId, "ROLE_USER", SECRET))
                        .header("X-User-Role", "ROLE_ADMIN").build());

        filter.filter(exchange, chain).block();

        assertThat(forwarded.get().getRequest().getHeaders().get("X-User-Role")).containsExactly("ROLE_USER");
    }

    @Test
    void publicRoutesPassWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/pets?page=0&items=9").build());

        filter.filter(exchange, chain).block();

        assertThat(forwarded.get()).isNotNull();
    }

    @Test
    void pathTraversalIsRejected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.GET, URI.create("http://localhost/images/../internal/users")).build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(forwarded.get()).isNull();
    }
}
