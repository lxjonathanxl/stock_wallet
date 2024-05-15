package com.example.stockData.exceptions;

public class StockExistsException extends RuntimeException{

    public StockExistsException(String message) {
        super(message);
    }
}
