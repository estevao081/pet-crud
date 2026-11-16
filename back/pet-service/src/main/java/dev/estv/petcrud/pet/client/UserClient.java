package dev.estv.petcrud.pet.client;

import dev.estv.petcrud.pet.dto.UserSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Busca os donos dos pets no user-service (uma chamada em lote por página, não uma por pet). */
@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestClient restClient;

    public UserClient(RestClient userRestClient) {
        this.restClient = userRestClient;
    }

    /**
     * Degradação graciosa: se o user-service estiver fora, devolve mapa vazio e os pets aparecem
     * sem os dados do dono, em vez de a home inteira falhar.
     */
    public Map<UUID, UserSummary> findSummaries(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            List<UserSummary> body = restClient.post()
                    .uri("/internal/users/summaries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.copyOf(ids))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserSummary>>() {
                    });
            if (body == null) {
                return Map.of();
            }
            return body.stream().collect(Collectors.toMap(UserSummary::id, Function.identity(), (a, b) -> a));
        } catch (RestClientException e) {
            log.warn("user-service indisponível; devolvendo pets sem dados do dono: {}", e.getMessage());
            return Map.of();
        }
    }
}
