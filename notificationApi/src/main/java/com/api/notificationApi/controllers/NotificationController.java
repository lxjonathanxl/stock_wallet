package com.api.notificationApi.controllers;

import com.api.notificationApi.dto.NotificationRequest;
import com.api.notificationApi.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping(produces="application/json")
public class NotificationController {

    private static final Logger controllerLogger = LoggerFactory.getLogger(NotificationController.class);
    private final EmailSenderService emailSenderService;

    public NotificationController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }


    @GetMapping("/get/result")
    public Boolean getResult() {
        Random random = new Random();
        Boolean result = random.nextBoolean();

        controllerLogger.info("api was called and will return: {}", result);

        return result;
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<Boolean> sendEmail(@Valid @RequestBody NotificationRequest notificationRequest,
                                            Errors errors) throws MessagingException, UnsupportedEncodingException {

        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            controllerLogger.error("Client sent notification request with invalid field " +
                    "message: {}", message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(false);
        }

        emailSenderService.sendEmail(notificationRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

}
