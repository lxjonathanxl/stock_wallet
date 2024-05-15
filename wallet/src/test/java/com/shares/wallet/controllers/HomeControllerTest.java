package com.shares.wallet.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.model.StockDisplay;
import com.shares.wallet.services.TransactionService;
import com.shares.wallet.services.UsersService;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@WithMockUser
public class HomeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UsersService usersService;

    private List<StockDisplay> stocks;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        request = new TransactionRequest(
                "TEST", BigDecimal.valueOf(2), BigDecimal.valueOf(20)
        );

        stocks = new ArrayList<>();
        stocks.add(new StockDisplay("AAPL", BigDecimal.valueOf(10),
                BigDecimal.valueOf(1), BigDecimal.valueOf(10)));
        stocks.add(new StockDisplay("TEST", BigDecimal.valueOf(2),
                BigDecimal.valueOf(20), BigDecimal.valueOf(10)));


    }

    @Test
    void home_Successful() throws Exception {
        // Arrange
        BigDecimal total = BigDecimal.valueOf(20);
        BigDecimal cash = BigDecimal.valueOf(1000);

        when(transactionService.displayStocks(anyString())).thenReturn(stocks);
        when(usersService.lookIntoCash(anyString())).thenReturn(cash);


        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index.html"))
                .andExpect(model().attributeExists("stocks", "total", "cash"))
                .andExpect(model().attribute("stocks", stocks))
                .andExpect(model().attribute("total", total))
                .andExpect(model().attribute("cash", cash));
    }

    @Test
    void home_Error_UsernameNotFoundException() throws Exception {
        // Arrange
        String errorMessage = "Error looking for user on database";
        when(transactionService.displayStocks(anyString())).thenThrow(new UsernameNotFoundException(errorMessage));

        // Act and Assert
        mockMvc.perform(get("/"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", errorMessage));
    }

    @Test
    void home_Error_JsonProcessingException() throws Exception {
        // Arrange
        String errorMessage = "Error looking for stock";
        when(transactionService.displayStocks(anyString())).thenThrow(new JsonProcessingException(errorMessage) {});

        // Act and Assert
        mockMvc.perform(get("/"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", errorMessage));

    }

    @Test
    void buyConfirmPost_Successful() throws Exception {
        //Arrange
        String message = "Stock added to portfolio";
        when(transactionService.buy(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/homeBuyConfirm")
                        .sessionAttr("homeStockListRequest", stocks)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "20"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(view().name("redirect:/"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void buyConfirmPost_fail_sellConfirmRequestMismatchWithRequestInSession() throws Exception {
        //Arrange
        String message = "Invalid request";

        //Act and Assert
        mockMvc.perform(post("/homeBuyConfirm")
                        .sessionAttr("homeStockListRequest", stocks)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "40"),
                                        new BasicNameValuePair("price", "1"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(view().name("redirect:/"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void buyConfirmPost_fail_invalidSymbol() throws Exception {
        //Arrange
        String message = "symbol field empty";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/homeBuyConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", ""),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "20"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buyConfirmPost_fail_invalidShares() throws Exception {
        //Arrange
        String message = "shares field invalid";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/homeBuyConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "Invalid"),
                                        new BasicNameValuePair("price", "20"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buyConfirmPost_fail_invalidPrice() throws Exception {
        //Arrange
        String message = "price field invalid";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/homeBuyConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "Invalid"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sellConfirmPost_Successful() throws Exception {
        //Arrange
        String message = "Stock sold";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/homeSellConfirm")
                        .sessionAttr("homeStockListRequest", stocks)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "20"),
                                        new BasicNameValuePair("total", "10")
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
        mockMvc.perform(post("/homeSellConfirm")
                        .sessionAttr("homeStockListRequest", stocks)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "40"),
                                        new BasicNameValuePair("price", "1"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(view().name("redirect:/"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void sellConfirmPost_fail_invalidSymbol() throws Exception {
        //Arrange
        String message = "symbol field empty";
        when(transactionService.sell(any(TransactionRequest.class), anyString()))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/homeSellConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", ""),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "20"),
                                        new BasicNameValuePair("total", "10")
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
        mockMvc.perform(post("/homeSellConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "Invalid"),
                                        new BasicNameValuePair("price", "20"),
                                        new BasicNameValuePair("total", "10")
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
        mockMvc.perform(post("/homeSellConfirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("symbol", "TEST"),
                                        new BasicNameValuePair("shares", "2"),
                                        new BasicNameValuePair("price", "Invalid"),
                                        new BasicNameValuePair("total", "10")
                                ))
                        )))
                .andExpect(status().isBadRequest());
    }
}
