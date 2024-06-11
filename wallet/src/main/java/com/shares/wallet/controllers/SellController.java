package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class SellController {

    private final static Logger sellControllerLogger = LoggerFactory.getLogger(SellController.class);
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
            sellControllerLogger.warn("Client tried to quote stock to sell" +
                    " but some field on the form had invalid value " +
                    "message: {}", message);
            return "redirect:/sell";
        }

        String message;
        BigDecimal quant = request.getShares();

        try {
            StockQuote stock = transactionService.lookUpStock(request);
            if (stock.getSymbol() == null) {
                message = "Invalid stock symbol";
                redirectAttributes.addFlashAttribute("message", message);
                sellControllerLogger.warn("Client tried to quote stock to sell " +
                        "but had invalid stock symbol on request," +
                        "Stock: {}", request.getSymbol());
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

        } catch (JsonProcessingException | IllegalArgumentException error) {
            message = "Error looking for stock";
            redirectAttributes.addFlashAttribute("message", message);
            sellControllerLogger.error("Client tried to quote stock to sell " +
                    "error problems finding stock through api," +
                    "Stock: {}", request.getSymbol(), error);
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

        String username = principal.getName();

        if (errors.hasErrors()) {
            sellControllerLogger.warn("user tried to confirm sell transaction but confirmation request had invalid value in field, " +
                    "stock: {}, user: {}, request: {}", request.getSymbol(), username, request);
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sell";
        }

        TransactionRequest sellConfirmRequest = (TransactionRequest) session.getAttribute("sellConfirmRequest");
        session.removeAttribute("sellConfirmRequest");

        if(!sellConfirmRequest.equals(request)) {
            sellControllerLogger.warn("user tried to confirm sell transaction but confirmation request stock didn't matched stock saved in session, " +
                    "stock: {}, user: {}, requestConfirmation: {}, requestInSession: {}",
                    request.getSymbol(), username, request, sellConfirmRequest);
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sell";

        }

        String message = transactionService.sell(sellConfirmRequest, username);
        redirectAttributes.addFlashAttribute("message", message);
        sellControllerLogger.info("sell transaction result message: {}, stock: {}, user: {}",
                message ,request.getSymbol(), username);
        return "redirect:/";

    }
}
