package dev.estv.petcrud.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.estv.petcrud.gateway.util.JsonErrors;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate limit por IP, mesmos limites do monólito (10 req/min, configurável).
 * Migrou do Bucket4j+Hazelcast (que vivia dentro do backend) para buckets em memória:
 * agora existe UM ponto de entrada, então não há mais necessidade de estado distribuído.
 * Se o gateway for replicado, troque o Cache por um ProxyManager (Redis/Hazelcast).
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    private final long capacity;
    private final Duration period;

    public RateLimitFilter(@Value("${gateway.rate-limit.capacity:10}") long capacity,
                           @Value("${gateway.rate-limit.period-seconds:60}") long periodSeconds) {
        this.capacity = capacity;
        this.period = Duration.ofSeconds(periodSeconds);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Bucket bucket = buckets.get(clientKey(exchange), k -> newBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set("X-Rate-Limit-Limit", String.valueOf(capacity));
        response.getHeaders().set("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (!probe.isConsumed()) {
            long retryAfter = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
            response.getHeaders().set(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
            return JsonErrors.write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
        }
        return chain.filter(exchange);
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(capacity, period).build())
                .build();
    }

    private String clientKey(ServerWebExchange exchange) {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        if (address == null || address.getAddress() == null) {
            return "unknown";
        }
        return address.getAddress().getHostAddress();
    }

    @Override
    public int getOrder() {
        return -2; // antes da autenticação, como no monólito
    }
}
