package dev.estv.petcrud.gateway.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/** Escreve erros no mesmo formato ApiResponse usado por todos os serviços. */
public final class JsonErrors {

    private JsonErrors() {
    }

    /** {@code message} deve ser uma constante nossa (não é escapada). */
    public static Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"success\":false,\"data\":null,\"message\":\"" + message
                + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
