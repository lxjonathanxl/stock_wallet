package com.shares.wallet.exceptions;

import jakarta.persistence.NoResultException;

public class HistoryNotFoundException extends NoResultException {
    public HistoryNotFoundException() {
        super();
    }

    public HistoryNotFoundException(String message) {
        super(message);
    }

}
