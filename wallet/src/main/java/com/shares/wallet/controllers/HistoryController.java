package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.exceptions.HistoryNotFoundException;
import com.shares.wallet.model.History;
import com.shares.wallet.model.StockDisplay;
import com.shares.wallet.services.TransactionService;
import com.shares.wallet.services.UsersService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HistoryController {

    private final TransactionService transactionService;
    public HistoryController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping("/history")
    public String home(Principal principal,
                       RedirectAttributes redirectAttributes,
                       Model model) {

        String message;
        String username = principal.getName();
        List<History> history = new ArrayList<>();

        try {
            history = transactionService.displayUserHistory(username);
        } catch (UsernameNotFoundException userError) {
            message = "Error looking for user history, username not found in database";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        } catch (HistoryNotFoundException historyError) {
            message = "Error looking for user history, history not found in database";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        model.addAttribute("history", history);
        return "history.html";

    }
}
