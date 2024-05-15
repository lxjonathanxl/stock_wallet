package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.services.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class SellController {

    private final TransactionService transactionService;

    public SellController(TransactionService transactionService, HttpServletRequest request) {
        this.transactionService = transactionService;
    }

    @GetMapping("/sell")
    public String sellGet(Model model, Principal principal) {

        String username = principal.getName();

        List<Stocks> stocks = transactionService.findUserStocks(username);
        model.addAttribute("stocks", stocks);
        return "sell.html";
    }

    @PostMapping("/sell")
    public String sellPost(@Valid QuoteRequest request, Errors errors,
                           HttpSession session,
                           Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sell";
        }

        String message;
        BigDecimal quant = request.getShares();

        try {
            StockQuote stock = transactionService.lookUpStock(request);
            if (stock.getSymbol() == null) {
                message = "Invalid stock symbol";
                redirectAttributes.addFlashAttribute("message", message);
                return "redirect:/sell";
            }

            String symbol = stock.getSymbol();
            model.addAttribute("stockName", symbol);

            model.addAttribute("shares", quant);

            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(stock.getPrice()));
            model.addAttribute("stockPrice", price);
            BigDecimal total = quant.multiply(price);

            model.addAttribute("total", total);

            TransactionRequest sellConfirm = new TransactionRequest(symbol, quant, price);
            session.setAttribute("sellConfirmRequest", sellConfirm);

        } catch (JsonProcessingException | IllegalArgumentException e) {
            message = "Error looking for stock";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sell";
        }

        return "sell.html";
    }

    @PostMapping("/sellConfirm")
    public String PurchaseConfirmation(
            @Valid @ModelAttribute TransactionRequest request,
            Principal principal, Errors errors, HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sell";
        }

        TransactionRequest sellConfirm = (TransactionRequest) session.getAttribute("sellConfirmRequest");
        session.removeAttribute("sellConfirmRequest");

        if(!sellConfirm.equals(request)) {
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sell";

        }

        String username = principal.getName();

        String message = transactionService.sell(request, username);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";

    }
}
