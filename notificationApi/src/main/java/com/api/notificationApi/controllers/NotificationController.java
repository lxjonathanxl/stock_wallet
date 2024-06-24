package com.api.notificationApi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping(produces="application/json")
public class NotificationController {

    @GetMapping("/get/result")
    public Boolean getResult() {

        Random random = new Random();

        return random.nextBoolean();
    }

}
