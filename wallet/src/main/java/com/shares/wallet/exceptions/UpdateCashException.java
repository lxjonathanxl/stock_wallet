package com.shares.wallet.exceptions;

import org.springframework.dao.DataAccessException;

public class UpdateCashException extends DataAccessException {

    public UpdateCashException(String msg) {
        super(msg);
    }

    public UpdateCashException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
