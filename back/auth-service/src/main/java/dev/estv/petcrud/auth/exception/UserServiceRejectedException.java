package dev.estv.petcrud.auth.exception;

import org.springframework.http.HttpStatusCode;

/** O user-service recusou a operação (ex.: 409 e-mail já existe, 400 dados inválidos). Repassamos status e mensagem. */
public class UserServiceRejectedException extends RuntimeException {

    private final HttpStatusCode status;

    public UserServiceRejectedException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
