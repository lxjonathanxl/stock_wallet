package com.shares.wallet.controllers;

import com.shares.wallet.dto.RegistrationRequest;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.services.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final static Logger registrationControllerLogger = LoggerFactory.getLogger(RegistrationController.class);
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute RegistrationRequest request, Errors errors,
            RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            registrationControllerLogger.warn("Client tried to register but some field on the form had invalid value " +
                    "message: {}", message);
            return "redirect:/register";
        }

        MessageController registrationResult = registrationService.register(request);
        redirectAttributes.addFlashAttribute("message", registrationResult.getMessage());

        if (!registrationResult.getSucceeded()) {
            registrationControllerLogger.warn("Client registration failed " +
                    "message: {}", registrationResult.getMessage());
            return "redirect:/register";
        }

        registrationControllerLogger.info("new user registered, username: {}",
                request.getUsername());
        return "redirect:/login";
    }

    @GetMapping
    public String registerGet() {
        return "register.html";
    }
}






