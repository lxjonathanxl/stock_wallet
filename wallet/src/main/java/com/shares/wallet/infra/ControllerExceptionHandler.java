package com.shares.wallet.infra;

import com.shares.wallet.exceptions.AlterUserStockException;
import com.shares.wallet.exceptions.ServerErrorException;
import com.shares.wallet.exceptions.UpdateCashException;
import com.shares.wallet.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({AlterUserStockException.class, UpdateCashException.class, UserNotFoundException.class})
    public String transactionExceptions(Exception exception,
            RedirectAttributes redirectAttributes) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }

    @ExceptionHandler(ServerErrorException.class)
    public String ServerErrorExceptions(Exception exception,
                                        RedirectAttributes redirectAttributes) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/login";
    }
}
