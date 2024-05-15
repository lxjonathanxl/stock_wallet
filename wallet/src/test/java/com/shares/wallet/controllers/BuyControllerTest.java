package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.services.TransactionService;
import com.shares.wallet.services.UsersService;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BuyController.class)
public class BuyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    private TransactionService transactionService;

    private QuoteRequest request;

    private TransactionRequest buyConfirmRequest;

    @BeforeEach
    void setUp() {
        request = QuoteRequest
                .builder()
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        buyConfirmRequest = new TransactionRequest(
                "TEST", BigDecimal.valueOf(2), BigDecimal.valueOf(20));
    }

    @Test
    @WithMockUser
    void buyPost_Successful() throws Exception {
        //Arrange
        StockQuote stockQuoteTest = mock(StockQuote.class);

        when(stockQuoteTest.getSymbol()).thenReturn("TEST");
        when(stockQuoteTest.getPrice()).thenReturn("20.00");
        when(transactionService.lookUpStock(request))
                .thenReturn(stockQuoteTest);

        //Act and Assert
        BigDecimal price = BigDecimal.valueOf(20.00);
        BigDecimal quant = request.getShares();
        BigDecimal total = quant.multiply(price);

        mockMvc.perform(post("/buy")
                        .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair(
                                        "symbol", "TEST"),
                                new BasicNameValuePair(
                                        "shares", "2"))))))
                .andExpect(view().name("buy.html"))
                .andExpect(model().attribute("stockName", "TEST"))
                .andExpect(model().attribute("shares", request.getShares()))
                .andExpect(model().attribute("stockPrice", price))
                .andExpect(model().attribute("total", total))
                .andExpect(request().sessionAttribute("buyConfirmRequest", buyConfirmRequest));
    }

    @Test
    @WithMockUser
    void buyPost_fail_InvalidRequest_symbolEmpty() throws Exception {
        //Arrange

        String errorMessage = "symbol field empty";
        mockMvc.perform(post("/buy")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", ""),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/buy"))
                .andExpect(redirectedUrl("/buy"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void buyPost_fail_InvalidRequest_sharesEmpty() throws Exception {
        //Arrange

        String errorMessage = "Shares field empty";
        mockMvc.perform(post("/buy")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "Test"),
                                        new BasicNameValuePair(
                                                "shares", ""))))))
                .andExpect(view().name("redirect:/buy"))
                .andExpect(redirectedUrl("/buy"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void buyPost_fail_InvalidRequest_sharesNonDigitsValue() throws Exception {
        //Arrange

        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.math.BigDecimal'; Character I is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.";

        mockMvc.perform(post("/buy")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "Test"),
                                        new BasicNameValuePair(
                                                "shares", "InvalidValue"))))))
                .andExpect(view().name("redirect:/buy"))
                .andExpect(redirectedUrl("/buy"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void buyPost_fail_ErrorLookingForStock() throws Exception {
        //Arrange
        String errorMessage = "Error looking for stock";

        when(transactionService.lookUpStock(request))
                .thenThrow(JsonProcessingException.class);

        //Act and Assert
        mockMvc.perform(post("/buy")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/buy"))
                .andExpect(redirectedUrl("/buy"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void buyPost_fail_InvalidSymbol() throws Exception {
        //Arrange
        String errorMessage = "Invalid stock symbol";

        StockQuote stockQuoteTest = mock(StockQuote.class);

        when(stockQuoteTest.getSymbol()).thenReturn(null);
        when(transactionService.lookUpStock(request))
                .thenReturn(stockQuoteTest);

        //Act and Assert
        mockMvc.perform(post("/buy")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/buy"))
                .andExpect(redirectedUrl("/buy"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void buyConfirmPost_Successful() throws Exception {
        //Arrange

        String message = "Stock added to portfolio";
        when(transactionService.buy(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/buyConfirm")
                        .sessionAttr("buyConfirmRequest", buyConfirmRequest)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("symbol", "TEST"),
                                new BasicNameValuePair("shares", "2"),
                                new BasicNameValuePair("price", "20")
                        ))
                )))
                .andExpect(view().name("redirect:/"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    @WithMockUser
    void buyConfirmPost_fail_buyRequestMismatchWithRequestSavedInSession() throws Exception {
        //Arrange

        String message = "Invalid request";

        //Act and Assert
        mockMvc.perform(post("/buyConfirm")
                        .sessionAttr("buyConfirmRequest", buyConfirmRequest)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "40"),
                                        new BasicNameValuePair("price", "1")
                                ))
                        )))
                .andExpect(view().name("redirect:/buy"))
                .andExpect(redirectedUrl("/buy"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    @WithMockUser
    void buyConfirmPost_fail_invalidSymbol() throws Exception {
        //Arrange
        String message = "symbol field empty";
        when(transactionService.buy(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/buyConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", ""),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "20")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void buyConfirmPost_fail_invalidShares() throws Exception {
        //Arrange
        String message = "shares field invalid";
        when(transactionService.buy(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/buyConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "Invalid"),
                                        new BasicNameValuePair("price", "20")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void buyConfirmPost_fail_invalidPrice() throws Exception {
        //Arrange
        String message = "price field invalid";
        when(transactionService.buy(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/buyConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "Invalid")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }








}
