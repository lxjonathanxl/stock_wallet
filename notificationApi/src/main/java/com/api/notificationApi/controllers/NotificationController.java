package com.api.notificationApi.controllers;

import com.api.notificationApi.service.EmailSenderService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
    public String sendEmail(@RequestBody Map<String, String> request) throws MessagingException, UnsupportedEncodingException {
        String email = request.get("email");

        // Call the sendEmail method to send an email
        String subject = "Hello from Spring Boot";
        String content = "<p>Hello,</p><p>This is a test email sent from Spring Boot.</p>";

        try {
            emailSenderService.sendEmail(email, subject, content);
            return "Email sent successfully.";
        } catch (MessagingException | UnsupportedEncodingException e) {
            return "Failed to send email. Error: " + e.getMessage();
        }
    }

}
