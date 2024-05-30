package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.services.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class BuyController {

    private final TransactionService transactionService;

    public BuyController(TransactionService transactionService, HttpServletRequest request) {
        this.transactionService = transactionService;
    }

    @GetMapping("/buy")
    public String buyGet() {
        return "buy.html";
    }

    @PostMapping("/buy")
    public String buyPost(@Valid @ModelAttribute QuoteRequest request, Errors errors,
                          HttpSession session,
                          Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/buy";
        }

        String message;
        BigDecimal quant = request.getShares();

        try {
            StockQuote stock = transactionService.lookUpStock(request);
            if (stock.getSymbol() == null) {
                message = "Invalid stock symbol";
                redirectAttributes.addFlashAttribute("message", message);
                return "redirect:/buy";
            }

            String symbol = stock.getSymbol();
            model.addAttribute("stockName", symbol);

            model.addAttribute("shares", quant);

            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(stock.getPrice()));
            model.addAttribute("stockPrice", price);

            BigDecimal total = quant.multiply(price);
            model.addAttribute("total", total);

            TransactionRequest buyConfirm = new TransactionRequest(symbol, quant, price);

            session.setAttribute("buyConfirmRequest", buyConfirm);

        } catch (JsonProcessingException | IllegalArgumentException e) {
            message = "Error looking for stock";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/buy";
        }

        return "buy.html";
    }

    @PostMapping("/buyConfirm")
    public String PurchaseConfirmation(
            @Valid @ModelAttribute TransactionRequest request,
            Principal principal, Errors errors, HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/buy";
        }

        TransactionRequest buyConfirmRequest = (TransactionRequest) session.getAttribute("buyConfirmRequest");
        session.removeAttribute("buyConfirmRequest");

        if (!buyConfirmRequest.equals(request)) {
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/buy";

        }

        String username = principal.getName();

        String message = transactionService.buy(buyConfirmRequest, username);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";

    }

}
