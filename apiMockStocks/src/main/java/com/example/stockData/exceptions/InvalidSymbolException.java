package com.example.stockData.exceptions;

public class InvalidSymbolException extends RuntimeException{

    public InvalidSymbolException(String message) {
        super(message);
    }
}
