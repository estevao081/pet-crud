package dev.estv.petcrud.user.client;

import dev.estv.petcrud.user.exception.PetServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/** Substitui o antigo "cascade = REMOVE" do JPA: quem apaga os pets do usuário agora é o pet-service. */
@Component
public class PetClient {

    private final RestClient restClient;

    public PetClient(RestClient petRestClient) {
        this.restClient = petRestClient;
    }

    /** Idempotente: se não houver pets, não faz nada. Pode ser repetido com segurança. */
    public void deletePetsOwnedBy(UUID ownerId) {
        try {
            restClient.delete()
                    .uri("/internal/pets/owner/{ownerId}", ownerId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new PetServiceUnavailableException(e);
        }
    }
}
