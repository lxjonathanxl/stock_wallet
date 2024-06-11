package com.shares.wallet.infra;

import com.shares.wallet.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final static Logger controllerExceptionHandler = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler({AlterUserStockException.class, UpdateCashException.class, UserNotFoundException.class})
    public String transactionExceptions(Exception exception,
            RedirectAttributes redirectAttributes) {

        controllerExceptionHandler.error("Error caught by controller advice referent to transaction",
                exception);
        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }

    @ExceptionHandler(ServerErrorException.class)
    public String ServerErrorExceptions(Exception exception,
                                        RedirectAttributes redirectAttributes) {

        controllerExceptionHandler.error("Error caught by controller advice referent to serverError " +
                        "probably user wasn't found on database",
                exception);
        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/login";
    }

    @ExceptionHandler(HistoryNotFoundException.class)
    public String HistoryNotFoundException(Exception exception,
                                        RedirectAttributes redirectAttributes) {

        controllerExceptionHandler.error("Error caught by controller advice referent to user history",
                exception);
        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }
}
