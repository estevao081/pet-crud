package dev.estv.petcrud.gateway;

import dev.estv.petcrud.gateway.filter.RateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter(2, 60);
    private final GatewayFilterChain chain = ex -> Mono.empty();

    private MockServerWebExchange from(String ip) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/pets")
                .remoteAddress(new InetSocketAddress(ip, 5000)).build());
    }

    @Test
    void blocksAfterCapacityIsExhausted() {
        filter.filter(from("10.0.0.1"), chain).block();
        filter.filter(from("10.0.0.1"), chain).block();

        MockServerWebExchange third = from("10.0.0.1");
        filter.filter(third, chain).block();

        assertThat(third.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(third.getResponse().getHeaders().getFirst("Retry-After")).isNotNull();
    }

    @Test
    void limitIsPerClientIp() {
        filter.filter(from("10.0.0.1"), chain).block();
        filter.filter(from("10.0.0.1"), chain).block();
        filter.filter(from("10.0.0.1"), chain).block();

        MockServerWebExchange other = from("10.0.0.2");
        filter.filter(other, chain).block();

        assertThat(other.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
