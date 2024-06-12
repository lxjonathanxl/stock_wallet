package com.example.stockData.infra;

import com.example.stockData.exceptions.InvalidSymbolException;
import com.example.stockData.exceptions.StockExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final static Logger controllerExceptionHandlerLogger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler({InvalidSymbolException.class, StockExistsException.class})
    private ResponseEntity<String> invalidSymbol(Exception exception) {

        controllerExceptionHandlerLogger.error("Error caught by controller exception handler", exception);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }
}
