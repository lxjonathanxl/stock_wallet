package com.example.stockData.repository;

import com.example.stockData.model.StockQuote;
import com.example.stockData.repositories.StocksRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@DataMongoTest
@Testcontainers
public class StocksRepositoryTest {

    static MongoDBContainer mongoDB = new MongoDBContainer(DockerImageName.parse("mongo:5.0.2"));

    @BeforeAll
    static void beforeAll() {
        mongoDB.start();
    }

    @AfterAll
    static void afterAll() {
        mongoDB.stop();
    }

    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private StocksRepository stocksRepository;

    @Test
    public void StocksRepository_deleteBySymbol_returnStockQuote() {
        //Assert
        StockQuote stockTest = new StockQuote();
        mongoTemplate.save(stockTest);

        //Act
        Boolean stockConfirmationBefore = stocksRepository.existsBySymbol(stockTest.getSymbol());
        StockQuote stockResult = stocksRepository.deleteBySymbol(stockTest.getSymbol());
        Boolean stockConfirmationAfter = stocksRepository.existsBySymbol(stockTest.getSymbol());

        //Assert
        Assertions.assertThat(stockResult).isNotNull();
        Assertions.assertThat(stockResult).isEqualTo(stockTest);
        Assertions.assertThat(stockConfirmationBefore).isTrue();
        Assertions.assertThat(stockConfirmationAfter).isFalse();
    }

    @Test
    public void StocksRepository_findBySymbol_returnStockQuote() {
        //Assert
        StockQuote stockTest = new StockQuote();
        mongoTemplate.save(stockTest);

        //Act
        StockQuote stockResult = stocksRepository.findBySymbol(stockTest.getSymbol());

        //Assert
        Assertions.assertThat(stockResult).isNotNull();
        Assertions.assertThat(stockResult).isEqualTo(stockTest);

        //Cleaning test from database
        stocksRepository.deleteBySymbol(stockTest.getSymbol());
    }

    @Test
    public void StocksRepository_existBySymbol_returnBoolean() {
        //Assert
        StockQuote stockTest = new StockQuote();
        mongoTemplate.save(stockTest);

        //Act
        Boolean stockResult = stocksRepository.existsBySymbol(stockTest.getSymbol());

        //Assert
        Assertions.assertThat(stockResult).isNotNull();
        Assertions.assertThat(stockResult).isTrue();

        //Cleaning test from database
        stocksRepository.deleteBySymbol(stockTest.getSymbol());
    }

}
