package com.example.stockData.infra;

import com.example.stockData.exceptions.InvalidSymbolException;
import com.example.stockData.exceptions.StockExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({InvalidSymbolException.class, StockExistsException.class})
    private ResponseEntity<String> invalidSymbol(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }
}
