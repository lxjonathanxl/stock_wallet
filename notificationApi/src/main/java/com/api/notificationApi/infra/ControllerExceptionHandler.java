package com.api.notificationApi.infra;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.UnsupportedEncodingException;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final static Logger controllerExceptionLogger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler({MessagingException.class, UnsupportedEncodingException.class})
    private ResponseEntity<Boolean> badResponseFromEmailSender(Exception exception) {

        controllerExceptionLogger.error("Error caught by controller exception handler", exception);

        return new ResponseEntity<>(false, HttpStatus.BAD_GATEWAY);
    }
}
