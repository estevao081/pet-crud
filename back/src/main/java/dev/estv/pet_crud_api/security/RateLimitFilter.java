package dev.estv.pet_crud_api.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;

    private final BucketConfiguration bucketConfiguration =
            BucketConfiguration.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(10)
                                    .refillGreedy(10, Duration.ofMinutes(1))
                                    .build()
                    )
                    .build();

    public RateLimitFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getRemoteAddr();

        ConsumptionProbe probe = proxyManager
                .builder()
                .build(key, () -> bucketConfiguration)
                .tryConsumeAndReturnRemaining(1);

        response.setHeader(
                "X-Rate-Limit-Limit",
                "10"
        );

        response.setHeader(
                "X-Rate-Limit-Remaining",
                String.valueOf(probe.getRemainingTokens())
        );

        if (!probe.isConsumed()) {

            long retryAfterSeconds =
                    probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setHeader(
                    "Retry-After",
                    String.valueOf(retryAfterSeconds)
            );

            response.setStatus(429);

            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "status":429,
                        "message":"Too many requests"
                    }
                    """);

            return;
        }

        filterChain.doFilter(request, response);
    }
}