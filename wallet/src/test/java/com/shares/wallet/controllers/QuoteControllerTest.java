package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(QuoteController.class)
public class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private QuoteRequest request;

    @BeforeEach
    void setUp() {
        request = QuoteRequest
                .builder()
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();
    }

    @Test
    @WithMockUser
    void quotePost_Successful() throws Exception {
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

        mockMvc.perform(post("/quote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("quote.html"))
                .andExpect(model().attribute("stockName", "TEST"))
                .andExpect(model().attribute("shares", request.getShares()))
                .andExpect(model().attribute("stockPrice", price))
                .andExpect(model().attribute("total", total));
    }

    @Test
    @WithMockUser
    void quotePost_fail_InvalidRequest_symbolEmpty() throws Exception {
        //Arrange

        String errorMessage = "symbol field empty";
        mockMvc.perform(post("/quote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", ""),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/quote"))
                .andExpect(redirectedUrl("/quote"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void quotePost_fail_InvalidRequest_sharesEmpty() throws Exception {
        //Arrange

        String errorMessage = "Shares field empty";
        mockMvc.perform(post("/quote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "Test"),
                                        new BasicNameValuePair(
                                                "shares", ""))))))
                .andExpect(view().name("redirect:/quote"))
                .andExpect(redirectedUrl("/quote"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void quotePost_fail_InvalidRequest_sharesNonDigitsValue() throws Exception {
        //Arrange

        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.math.BigDecimal'; Character I is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.";

        mockMvc.perform(post("/quote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "Test"),
                                        new BasicNameValuePair(
                                                "shares", "InvalidValue"))))))
                .andExpect(view().name("redirect:/quote"))
                .andExpect(redirectedUrl("/quote"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void quotePost_fail_ErrorLookingForStock() throws Exception {
        //Arrange
        String errorMessage = "Error looking for stock";

        when(transactionService.lookUpStock(request))
                .thenThrow(JsonProcessingException.class);

        //Act and Assert
        mockMvc.perform(post("/quote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/quote"))
                .andExpect(redirectedUrl("/quote"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    @WithMockUser
    void quotePost_fail_InvalidSymbol() throws Exception {
        //Arrange
        String errorMessage = "Invalid stock symbol";

        StockQuote stockQuoteTest = mock(StockQuote.class);

        when(stockQuoteTest.getSymbol()).thenReturn(null);
        when(transactionService.lookUpStock(request))
                .thenReturn(stockQuoteTest);

        //Act and Assert
        mockMvc.perform(post("/quote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "symbol", "TEST"),
                                        new BasicNameValuePair(
                                                "shares", "2"))))))
                .andExpect(view().name("redirect:/quote"))
                .andExpect(redirectedUrl("/quote"))
                .andExpect(flash().attribute("message", errorMessage));
    }
}
