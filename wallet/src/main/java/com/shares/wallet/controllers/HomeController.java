package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.StockDisplay;
import com.shares.wallet.services.TransactionService;
import com.shares.wallet.services.UsersService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final TransactionService transactionService;
    private final UsersService usersService;
    public HomeController(TransactionService transactionService, UsersService usersService) {
        this.transactionService = transactionService;
        this.usersService = usersService;
    }
    @GetMapping("/")
    public String home(Principal principal,
                       RedirectAttributes redirectAttributes, HttpSession session,
                       Model model) throws JsonProcessingException {

        String message;
        String username = principal.getName();
        BigDecimal total = BigDecimal.ZERO;
        List<StockDisplay> stocks = new ArrayList<>();

        try {
            stocks = transactionService.displayStocks(username);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            message = "Error looking for stock";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/login";
        } catch (UsernameNotFoundException e) {
            message = "Error looking for user on database";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/login";
        }

        for (StockDisplay stock : stocks) {
            total = total.add(stock.getTotal());
        }

        BigDecimal cash = usersService.lookIntoCash(username);

        session.setAttribute("homeStockListRequest", stocks);

        model.addAttribute("stocks", stocks);
        model.addAttribute("total", total);
        model.addAttribute("cash", cash);
        return "index.html";
    }

    @PostMapping("/homeBuyConfirm")
    public String PurchaseConfirmation(
            @Valid @ModelAttribute TransactionRequest request,
            Principal principal, Errors errors, HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        ArrayList<StockDisplay> userStocks;
        userStocks = (ArrayList<StockDisplay>) session.getAttribute("homeStockListRequest");
        session.removeAttribute("homeStockListRequest");

        StockDisplay stockRequested = userStocks.stream()
                .filter(stockDisplay -> request.getSymbol().equals(stockDisplay.getName()))
                .findFirst()
                .orElse(null);

        if (stockRequested == null) {
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        TransactionRequest buyConfirm = new TransactionRequest(
                stockRequested.getName(), request.getShares(), stockRequested.getPrice());

        if (!buyConfirm.equals(request)) {
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        String username = principal.getName();

        String message = transactionService.buy(buyConfirm, username);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";

    }

    @PostMapping("/homeSellConfirm")
    public String sellConfirmation(
            @Valid @ModelAttribute TransactionRequest request,
            Principal principal, Errors errors, HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            String message = errors.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        ArrayList<StockDisplay> userStocks;
        userStocks = (ArrayList<StockDisplay>) session.getAttribute("homeStockListRequest");
        session.removeAttribute("homeStockListRequest");

        StockDisplay stockRequested = userStocks.stream()
                .filter(stockDisplay -> request.getSymbol().equals(stockDisplay.getName()))
                .findFirst()
                .orElse(null);

        if (stockRequested == null) {
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        TransactionRequest sellConfirm = new TransactionRequest(
                stockRequested.getName(), request.getShares(), stockRequested.getPrice());

        if (!sellConfirm.equals(request)) {
            String message = "Invalid request";
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/";
        }

        String username = principal.getName();

        String message = transactionService.sell(sellConfirm, username);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";

    }

}
