package com.example.stockData.repositories;

import com.example.stockData.model.StockQuote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface StocksRepository extends CrudRepository<StockQuote, String> {

    StockQuote findBySymbol(String symbol);

    StockQuote deleteBySymbol(String symbol);

    Boolean existsBySymbol(String symbol);
}
