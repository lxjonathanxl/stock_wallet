package com.shares.wallet.controllers;

import com.shares.wallet.dto.ChangeEmailRequest;
import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.exceptions.ServerErrorException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.services.UsersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class ProfileController {

    private final static Logger profileControllerLogger = LoggerFactory.getLogger(ProfileController.class);
    private final UsersService usersService;
    public ProfileController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/profile")
    public String getProfile(Principal principal,
                             Model model) {

        String username = principal.getName();
        model.addAttribute("username", username);
        return "profile.html";
    }

    @PostMapping("/profileUsername")
    public String changeUsername(@RequestParam("username") String newUsername,
                                 @RequestParam("password") String password,
                                 Principal principal, RedirectAttributes redirectAttributes,
                                 HttpServletRequest request, HttpServletResponse response) {
        String username = principal.getName();
        MessageController message;

        message = usersService.changeUsername(username, password, newUsername);
        redirectAttributes.addFlashAttribute("message", message.getMessage());

        if (!message.getSucceeded()) {
            profileControllerLogger.warn("User tried to change username but process failed, " +
                    "original username: {}, attempted new username: {}", username, newUsername);
            return "redirect:/profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login";

    }

    @PostMapping("/profileEmail")
    public String changeEmail(@Valid @ModelAttribute ChangeEmailRequest changeEmailRequest,
                              Errors errors,
                              Principal principal, RedirectAttributes redirectAttributes
                              ) {

        if (errors.hasErrors()) {
            profileControllerLogger.warn("Client tried to change email but used invalid email on field" +
                    " email:{}", changeEmailRequest.getEmail());
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/profile";
        }

        String username = principal.getName();
        String newEmail = changeEmailRequest.getEmail();
        String password = changeEmailRequest.getPassword();
        MessageController message;

        message = usersService.changeEmail(username, password, newEmail);
        redirectAttributes.addFlashAttribute("message", message.getMessage());

        if (!message.getSucceeded()) {
            profileControllerLogger.warn("User tried to change email but process failed, " +
                    "username: {}, attempted new email: {}", username, newEmail);
            return "redirect:/profile";
        }

        return "redirect:/profile";

    }
    @PostMapping("/profilePassword")
    public String changePassword(@RequestParam("password") String oldPassword,
                                 @RequestParam("new_password") String newPassword,
                                 @RequestParam("confirm_new_password") String confirmNewPassword,
                                 Principal principal, RedirectAttributes redirectAttributes,
                                 HttpServletRequest request, HttpServletResponse response) {

        String username = principal.getName();

        if (!newPassword.equals(confirmNewPassword)) {
            profileControllerLogger.warn("user tried to change password but password on request doesn't match user password on database, " +
                    "user: {}", username);
            String message = "new password and confirmation mismatch";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/profile";
        }

        MessageController message;

        message = usersService.changePassword(username, oldPassword, newPassword);
        redirectAttributes.addFlashAttribute("message", message.getMessage());

        if (!message.getSucceeded()) {
            profileControllerLogger.warn("User tried to change password but process failed, " +
                    "username: {}, message: {}", username, message.getMessage());
            return "redirect:/profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login";

    }

    @PostMapping("/profileCash")
    public String addCashController(@RequestParam("cash") BigDecimal cashToAdd,
                                    @RequestParam("password") String password,
                                    RedirectAttributes redirectAttributes, Principal principal) {

        String username = principal.getName();

        String message = usersService.changeCashUserProfile(username, password, cashToAdd);

        redirectAttributes.addFlashAttribute("message", message);
        profileControllerLogger.info("result of user adding cash to profile, " +
                "message: {}, user: {}", message, username);
        return "redirect:/profile";
    }
}
