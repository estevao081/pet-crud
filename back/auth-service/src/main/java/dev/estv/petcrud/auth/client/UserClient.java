package dev.estv.petcrud.auth.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.estv.petcrud.auth.dto.UserClientDTOs.CreateUserRequest;
import dev.estv.petcrud.auth.dto.UserClientDTOs.CreatedUser;
import dev.estv.petcrud.auth.dto.UserClientDTOs.UserCredentials;
import dev.estv.petcrud.auth.exception.UserServiceRejectedException;
import dev.estv.petcrud.auth.exception.UserServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/** Fala com a API INTERNA (/internal/**) do user-service, dona dos dados de usuário. */
@Component
public class UserClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public UserClient(RestClient userRestClient, ObjectMapper objectMapper) {
        this.restClient = userRestClient;
        this.objectMapper = objectMapper;
    }

    public Optional<UserCredentials> findByEmail(String email) {
        try {
            // {email} como variável de URI => valor codificado (um "+" no e-mail não vira espaço)
            UserCredentials body = restClient.get()
                    .uri("/internal/users/by-email?email={email}", email)
                    .retrieve()
                    .body(UserCredentials.class);
            return Optional.ofNullable(body);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new UserServiceUnavailableException(e);
        }
    }

    public CreatedUser create(CreateUserRequest request) {
        try {
            return restClient.post()
                    .uri("/internal/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                        throw new UserServiceRejectedException(res.getStatusCode(), extractMessage(res.getBody()));
                    })
                    .body(CreatedUser.class);
        } catch (UserServiceRejectedException e) {
            throw e;
        } catch (RestClientException e) {
            throw new UserServiceUnavailableException(e);
        }
    }

    private String extractMessage(InputStream body) {
        try {
            String message = objectMapper.readTree(body).path("message").asText(null);
            return message != null ? message : HttpStatus.BAD_REQUEST.getReasonPhrase();
        } catch (IOException e) {
            return HttpStatus.BAD_REQUEST.getReasonPhrase();
        }
    }
}
