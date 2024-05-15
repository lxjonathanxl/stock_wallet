package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.services.TransactionService;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(SellController.class)
@WithMockUser
public class SellControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private QuoteRequest request;

    private TransactionRequest sellConfirmRequest;

    @BeforeEach
    void setUp() {
        request = QuoteRequest
                .builder()
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        sellConfirmRequest = new TransactionRequest(
                "TEST", BigDecimal.valueOf(2), BigDecimal.valueOf(20)
        );
    }

    @Test
    void sellGet_successful() throws Exception {
        //Arrange
        Users userTest = Users
                .builder()
                .username("UserTest")
                .build();
        BigDecimal quant = BigDecimal.valueOf(2);

        List<Stocks> stocksTest = new ArrayList<>();
        stocksTest.add(new Stocks(userTest, quant, "TEST1"));
        stocksTest.add(new Stocks(userTest, quant, "TEST2"));

        when(transactionService.findUserStocks(anyString()))
                .thenReturn(stocksTest);

        //Act and Assert
        mockMvc.perform(get("/sell")
                .with(csrf()))
                .andExpect(view().name("sell.html"))
                .andExpect(model().attribute("stocks", stocksTest));
    }

    @Test
    void sellPost_successful() throws Exception {
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

        mockMvc.perform(post("/sell")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("sell.html"))
                .andExpect(model().attribute("stockName", "TEST"))
                .andExpect(model().attribute("shares", request.getShares()))
                .andExpect(model().attribute("stockPrice", price))
                .andExpect(model().attribute("total", total))
                .andExpect(request().sessionAttribute("sellConfirmRequest", sellConfirmRequest));
    }

    @Test
    void sellPost_fail_InvalidRequest_symbolEmpty() throws Exception {
        //Arrange

        String errorMessage = "symbol field empty";
        mockMvc.perform(post("/sell")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", ""),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/sell"))
                .andExpect(redirectedUrl("/sell"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    void sellPost_fail_InvalidRequest_sharesEmpty() throws Exception {
        //Arrange

        String errorMessage = "Shares field empty";
        mockMvc.perform(post("/sell")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "Test"),
                                        new BasicNameValuePair(
                                                "shares", ""))))))
                .andExpect(view().name("redirect:/sell"))
                .andExpect(redirectedUrl("/sell"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    void sellPost_fail_InvalidRequest_sharesNonDigitsValue() throws Exception {
        //Arrange

        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.math.BigDecimal'; Character I is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.";

        mockMvc.perform(post("/sell")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "Test"),
                                        new BasicNameValuePair(
                                                "shares", "InvalidValue"))))))
                .andExpect(view().name("redirect:/sell"))
                .andExpect(redirectedUrl("/sell"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    void sellPost_fail_ErrorLookingForStock() throws Exception {
        //Arrange
        String errorMessage = "Error looking for stock";

        when(transactionService.lookUpStock(request))
                .thenThrow(JsonProcessingException.class);

        //Act and Assert
        BigDecimal price = BigDecimal.valueOf(20.00);
        BigDecimal quant = request.getShares();
        BigDecimal total = quant.multiply(price);

        mockMvc.perform(post("/sell")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/sell"))
                .andExpect(redirectedUrl("/sell"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    void sellPost_fail_InvalidSymbol() throws Exception {
        //Arrange
        String errorMessage = "Invalid stock symbol";

        StockQuote stockQuoteTest = mock(StockQuote.class);

        when(stockQuoteTest.getSymbol()).thenReturn(null);
        when(transactionService.lookUpStock(request))
                .thenReturn(stockQuoteTest);

        //Act and Assert
        mockMvc.perform(post("/sell")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/sell"))
                .andExpect(redirectedUrl("/sell"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    void sellConfirmPost_Successful() throws Exception {
        //Arrange
        String message = "Stock sold";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/sellConfirm")
                        .sessionAttr("sellConfirmRequest", sellConfirmRequest)
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
    void sellConfirmPost_fail_sellConfirmRequestMismatchWithRequestInSession() throws Exception {
        //Arrange
        String message = "Invalid request";

        //Act and Assert
        mockMvc.perform(post("/sellConfirm")
                        .sessionAttr("sellConfirmRequest", sellConfirmRequest)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "40"),
                                        new BasicNameValuePair("price", "1")
                                ))
                        )))
                .andExpect(view().name("redirect:/sell"))
                .andExpect(redirectedUrl("/sell"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void sellConfirmPost_fail_invalidSymbol() throws Exception {
        //Arrange
        String message = "symbol field empty";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/sellConfirm")
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
    void sellConfirmPost_fail_invalidShares() throws Exception {
        //Arrange
        String message = "shares field invalid";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/sellConfirm")
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
    void sellConfirmPost_fail_invalidPrice() throws Exception {
        //Arrange
        String message = "price field invalid";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/sellConfirm")
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
