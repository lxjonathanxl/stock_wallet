package com.shares.wallet.exceptions;

public class ServerErrorException extends RuntimeException{

    public ServerErrorException(String message) {
        super(message);
    }
}
