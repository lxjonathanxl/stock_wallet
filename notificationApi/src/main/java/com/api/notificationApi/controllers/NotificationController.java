package com.api.notificationApi.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping(produces="application/json")
public class NotificationController {

    private static final Logger controllerLogger = LoggerFactory.getLogger(NotificationController.class);

    @GetMapping("/get/result")
    public Boolean getResult() {
        Random random = new Random();
        Boolean result = random.nextBoolean();

        controllerLogger.info("api was called and will return: {}", result);

        return result;
    }

}
