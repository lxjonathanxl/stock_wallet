package com.example.stockData.services;

import com.example.stockData.exceptions.InvalidSymbolException;
import com.example.stockData.exceptions.StockExistsException;
import com.example.stockData.model.StockQuote;
import com.example.stockData.repositories.StocksRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.bson.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private StocksRepository stocksRepository;

    @InjectMocks
    private StockService stockService;


    @Test
    public void StockService_save_returnJson_success() throws JsonProcessingException {
        //Arrange
        String symbol = "TEST";
        StockQuote stockQuote = new StockQuote(symbol);
        String jsonTest = stockQuote.convertToJson();


        when(stocksRepository.existsBySymbol(symbol))
                .thenReturn(false);
        when(stocksRepository.save(any(StockQuote.class)))
                .thenReturn(stockQuote);

        //Act
        String stockTestResult = stockService.save(symbol);

        //Assert
        Assertions.assertThat(stockTestResult).isNotNull();
        Assertions.assertThat(stockTestResult).isEqualTo(jsonTest);
    }

    @Test
    public void StockService_save_throwException_failInvalidSymbol() throws JsonProcessingException {
        //Arrange
        String symbolTest = "";
        //Act and Assert
        assertThatThrownBy(() -> stockService.save(symbolTest))
                .isInstanceOf(InvalidSymbolException.class)
                .hasMessageContaining("Invalid symbol");
    }

    @Test
    public void StockService_save_throwException_failStockAlreadyExists() throws JsonProcessingException {
        //Arrange
        String symbol = "TEST";
        when(stocksRepository.existsBySymbol(symbol))
                .thenReturn(true);

        //Act and Assert
        assertThatThrownBy(() -> stockService.save(symbol))
                .isInstanceOf(StockExistsException.class)
                .hasMessageContaining("Stock already exists");
    }

    @Test
    public void StockService_findBySymbol_returnJson_success() throws JsonProcessingException {
        //Arrange
        String symbol = "TEST";
        StockQuote stockQuote = new StockQuote(symbol);
        String jsonTest = stockQuote.convertToJson();

        when(stocksRepository.findBySymbol(symbol))
                .thenReturn(stockQuote);

        when(stocksRepository.existsBySymbol(symbol))
                .thenReturn(true);

        //Act
        String stockTestResult = stockService.findBySymbol(symbol);

        //Assert
        Assertions.assertThat(stockTestResult).isNotNull();
        Assertions.assertThat(stockTestResult).isEqualTo(jsonTest);
    }

    @Test
    public void StockService_findBySymbol_throwException_failInvalidSymbol() throws JsonProcessingException {
        //Arrange
        String symbolTest = "";

        //Act and Assert
        assertThatThrownBy(() -> stockService.findBySymbol(symbolTest))
                .isInstanceOf(InvalidSymbolException.class)
                .hasMessageContaining("Invalid symbol");
    }

    @Test
    public void StockService_save_throwException_failStockDontExists() throws JsonProcessingException {
        //Arrange
        String symbol = "NFLX";
        when(stocksRepository.existsBySymbol(symbol))
                .thenReturn(false);

        //Act and Assert
        assertThatThrownBy(() -> stockService.findBySymbol(symbol))
                .isInstanceOf(InvalidSymbolException.class)
                .hasMessageContaining("Stock not found");
    }
}
