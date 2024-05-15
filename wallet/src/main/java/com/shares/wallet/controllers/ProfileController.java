package com.shares.wallet.controllers;

import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.services.UsersService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class ProfileController {

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
            return "redirect:/profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login";

    }

    @PostMapping("/profilePassword")
    public String changePassword(@RequestParam("password") String oldPassword,
                                 @RequestParam("new_password") String newPassword,
                                 @RequestParam("confirm_new_password") String confirmNewPassword,
                                 Principal principal, RedirectAttributes redirectAttributes,
                                 HttpServletRequest request, HttpServletResponse response) {

        if (!newPassword.equals(confirmNewPassword)) {
            String message = "new password and confirmation mismatch";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/profile";
        }

        String username = principal.getName();
        MessageController message;

        message = usersService.changePassword(username, oldPassword, newPassword);
        redirectAttributes.addFlashAttribute("message", message.getMessage());

        if (!message.getSucceeded()) {
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

        if (!usersService.confirmPassword(username, password)) {
            redirectAttributes.addFlashAttribute("message", "wrong password");
            return "redirect:/profile";
        }

        try {
            BigDecimal userWallet = usersService.lookIntoCash(username);
            userWallet = userWallet.add(cashToAdd);
            usersService.updateCash(username, userWallet);
        } catch (DataAccessException | DatabaseException dataError) {
            //TODO LOGGING
            redirectAttributes.addFlashAttribute("message", "server error: " +
                    "handling users wallet");
            return "redirect:/profile";
        }

        redirectAttributes.addFlashAttribute("message", "cash added to wallet");
        return "redirect:/profile";
    }
}
