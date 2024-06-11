package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.services.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;

@Controller
public class QuoteController {

    private final static Logger quoteControllerLogger = LoggerFactory.getLogger(QuoteController.class);
    private final TransactionService transactionService;

    public QuoteController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/quote")
    public String quoteGet() {
        return "quote.html";
    }

    @PostMapping("/quote")
    public String quotePost(@Valid @ModelAttribute QuoteRequest request, Errors errors,
                          Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            quoteControllerLogger.warn("Client tried to quote stock" +
                    " but some field on the form had invalid value " +
                    "message: {}", message);
            return "redirect:/quote";
        }

        String message;
        BigDecimal quant = request.getShares();
        try {
            StockQuote stock = transactionService.lookUpStock(request);
            if (stock.getSymbol() == null) {
                quoteControllerLogger.warn("invalid stock symbol on request," +
                        "Stock: {}", request.getSymbol());
                message = "Invalid stock symbol";
                redirectAttributes.addFlashAttribute("message", message);
                return "redirect:/quote";
            }
            model.addAttribute("stockName", stock.getSymbol());
            model.addAttribute("shares", quant);
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(stock.getPrice()));
            model.addAttribute("stockPrice", price);
            BigDecimal total = quant.multiply(price);
            model.addAttribute("total", total);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            message = "Error looking for stock";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/quote";
        }

        return "quote.html";
    }
}
