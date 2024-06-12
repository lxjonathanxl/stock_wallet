package com.example.stockData.services;

import com.example.stockData.exceptions.InvalidSymbolException;
import com.example.stockData.exceptions.StockExistsException;
import com.example.stockData.model.StockQuote;
import com.example.stockData.repositories.StocksRepository;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final static Logger stockServiceLogger = LoggerFactory.getLogger(StockService.class);
    private final StocksRepository stocksRepository;

    public StockService(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }

    public String save(String symbol) {

        if (symbol == null || symbol.isEmpty() || symbol.contains(" ")) {
            stockServiceLogger.warn("User tried to save stock with invalid symbol, " +
                    "stock: {}", symbol);
            throw new InvalidSymbolException("Invalid symbol");
        }

        symbol = symbol.toUpperCase();

        if (stocksRepository.existsBySymbol(symbol)) {
            stockServiceLogger.warn("User tried to save stock that already saved on database, " +
                    "stock: {}", symbol);
            throw new StockExistsException("Stock already exists");
        }

        StockQuote stockToSave = new StockQuote(symbol);
        StockQuote stockSaved = stocksRepository.save(stockToSave);

        stockServiceLogger.info("User saved new stock on database, " +
                "stock: {}", symbol);
        return stockSaved.convertToJson();
    }

    public String findBySymbol(String symbol) {

        if (symbol == null || symbol.isEmpty() || symbol.contains(" ")) {
            stockServiceLogger.warn("User tried to find stock with invalid symbol, " +
                    "stock: {}", symbol);
            throw new InvalidSymbolException("Invalid symbol");
        }

        symbol = symbol.toUpperCase();

        if (!stocksRepository.existsBySymbol(symbol)) {
            stockServiceLogger.warn("User tried to find stock that doesn't exist on database, " +
                    "stock: {}", symbol);
            throw new InvalidSymbolException("Stock not found");
        }

        StockQuote stock = stocksRepository.findBySymbol(symbol);

        return stock.convertToJson();
    }
}
