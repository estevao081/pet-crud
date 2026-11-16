package dev.estv.petcrud.auth.exception;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(Throwable cause) {
        super("User service unavailable", cause);
    }
}
