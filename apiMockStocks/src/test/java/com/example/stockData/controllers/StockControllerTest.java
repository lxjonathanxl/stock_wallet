package com.example.stockData.controllers;

import com.example.stockData.config.SecurityConfig;
import com.example.stockData.exceptions.InvalidSymbolException;
import com.example.stockData.exceptions.StockExistsException;
import com.example.stockData.model.StockQuote;
import com.example.stockData.services.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.web.oauth2.client.OAuth2ClientSecurityMarker;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(StockController.class)
@WithMockUser(authorities = "SCOPE_stock.write")
public class StockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StockService stockService;

    @Test
    public void postSave_succeed() throws Exception {
        //Arrange
        StockQuote stockTest = new StockQuote("NFLX");
        String jsonTest = stockTest.convertToJson();

        when(stockService.save("NFLX"))
                .thenReturn(jsonTest);

        //Act and Assert
        mockMvc.perform(post("http://localhost:8090/save/NFLX")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    public void postSave_fail() throws Exception {
        //Arrange
        when(stockService.save("NFLX"))
                .thenThrow(new StockExistsException("Stock already exists"));

        //Act and Assert
        mockMvc.perform(post("http://localhost:8090/save/NFLX")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getStock_succeed() throws Exception {
        //Arrange
        StockQuote stockTest = new StockQuote("NFLX");
        String jsonTest = stockTest.convertToJson();

        when(stockService.findBySymbol("NFLX"))
                .thenReturn(jsonTest);

        //Act and Assert
        mockMvc.perform(get("http://localhost:8090/get/NFLX"))
                .andExpect(content().json(jsonTest))
                .andExpect(status().isOk());
    }

    @Test
    public void getStock_fail() throws Exception {
        //Arrange
        when(stockService.findBySymbol("NFLX"))
                .thenThrow(new InvalidSymbolException("Invalid Symbol"));

        //Act and Assert
        mockMvc.perform(get("http://localhost:8090/get/NFLX"))
                .andExpect(status().isBadRequest());
    }
}
