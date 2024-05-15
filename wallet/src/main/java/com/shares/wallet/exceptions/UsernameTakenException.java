package com.shares.wallet.exceptions;

public class UsernameTakenException extends IllegalStateException {
    public UsernameTakenException() {
        super();
    }

    public UsernameTakenException(String message) {
        super(message);
    }

    public UsernameTakenException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameTakenException(Throwable cause) {
        super(cause);
    }
}
